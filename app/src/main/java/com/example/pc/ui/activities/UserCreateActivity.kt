package com.example.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserRepository
import com.example.pc.databinding.ActivityCreateUserBinding
import com.example.pc.ui.adapters.FragmentsAdapter
import com.example.pc.ui.fragments.UserStepOne
import com.example.pc.ui.fragments.UserStepThree
import com.example.pc.ui.fragments.UserStepTwo
import com.example.pc.ui.viewmodels.UserModel
import com.example.pc.utils.*
import com.google.android.material.tabs.TabLayoutMediator
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
            UserStepThree()
        )
        fragmentsAdapter = FragmentsAdapter(
            this@UserCreateActivity,
            fragmentsList
        )

        super.onCreate(savedInstanceState)

        binding.apply {
            fragmentsViewPager.apply {

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
                            signUp(requestBody.build()).observe(this@UserCreateActivity) {
                                if (it.isNullOrBlank()) {
                                    //dialog ?
                                    this@UserCreateActivity.toast(SGN_FAILED, Toast.LENGTH_LONG)
                                    goToHomeFragment()
                                } else {
                                    this@UserCreateActivity.toast(
                                        SGN_SUCCESS,
                                        Toast.LENGTH_LONG
                                    )
                                    goToLoginPage()
                                }
                            }
                            isTurning.observe(this@UserCreateActivity) {
                                progressBar2.isVisible = it
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

    private fun goToHomeFragment() {
        finish()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginPage() {
        finish()
    }

}
