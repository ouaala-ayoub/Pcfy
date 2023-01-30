package alpha.company.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.UserRepository
import alpha.company.pc.databinding.ActivityCreateUserBinding
import alpha.company.pc.ui.adapters.FragmentsAdapter
import alpha.company.pc.ui.fragments.UserPolicyFragment
import alpha.company.pc.ui.fragments.UserStepOne
import alpha.company.pc.ui.fragments.UserStepThree
import alpha.company.pc.ui.fragments.UserStepTwo
import alpha.company.pc.ui.viewmodels.UserModel
import alpha.company.pc.utils.*
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import okhttp3.MultipartBody

private const val TAG = "UserCreateActivity"
private const val SGN_FAILED = "Erreur Inattendue"
private const val SGN_SUCCESS = "Compte crée avec succès"

class UserCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserBinding
    private lateinit var fragmentsList: List<Fragment>
    private lateinit var fragmentsAdapter: FragmentsAdapter
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userModel: UserModel
    val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)

    override fun onCreate(savedInstanceState: Bundle?) {

        //hiding the support bar
        supportActionBar?.hide()

        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        fragmentsList = listOf(
            UserStepOne(),
            UserStepTwo(),
            UserStepThree(),
            UserPolicyFragment()
        )
        fragmentsAdapter = FragmentsAdapter(
            this,
            fragmentsList
        )

        super.onCreate(savedInstanceState)

        binding.apply {
            fragmentsViewPager.apply {

                offscreenPageLimit = fragmentsList.size
                adapter = fragmentsAdapter
                TabLayoutMediator(progressTabBar, this, true) { _, _ ->
                    // to implement
                }.attach()
                isUserInputEnabled = false

                next.setOnClickListener {
                    fragmentsAdapter.onNextClicked(currentItem)

                    if (currentItem == fragmentsList.lastIndex) {
                        //check if the user is in the last step
                        userModel = UserModel(UserRepository(retrofitService))

                        userModel.apply {
                            Firebase.messaging.token.addOnCompleteListener { task ->
                                val token = task.result

                                signUp(requestBody.build()).observe(this@UserCreateActivity) { userId ->
                                    if (userId.isNullOrBlank()) {
                                        //dialog ?
                                        this@UserCreateActivity.toast(SGN_FAILED, Toast.LENGTH_LONG)
                                        goToHomeFragment()
                                    } else {
                                        registerToken(userId, token)
                                        this@UserCreateActivity.toast(
                                            SGN_SUCCESS,
                                            Toast.LENGTH_LONG
                                        )
                                        goToLoginPage()
                                    }
                                }

                            }

                            isTurning.observe(this@UserCreateActivity) { loading ->
                                progressBar2.isVisible = loading
                                changeUiEnabling(loading)
                            }
                        }

                    } else {
                        currentItem++
                    }

                }
                back.setOnClickListener {
                    if (currentItem == 0) {
                        makeDialog(
                            this@UserCreateActivity,
                            object : OnDialogClicked {
                                override fun onPositiveButtonClicked() {
                                    this@UserCreateActivity.onBackPressed()
                                }

                                override fun onNegativeButtonClicked() {}
                            },
                            getString(R.string.quit_signup_title),
                            getString(R.string.quit_signup_message)
                        ).show()
                    } else {
                        fragmentsAdapter.onBackClicked(currentItem)
                        currentItem--
//                        next.isActivated = true
                    }
                }

            }

            setContentView(binding.root)
        }

    }

    private fun changeUiEnabling(loading: Boolean) {
        binding.apply {
            next.isEnabled = !loading
            back.isEnabled = !loading
        }
    }

    private fun goToHomeFragment() {
        finish()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginPage() {
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
