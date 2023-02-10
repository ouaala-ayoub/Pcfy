package alpha.company.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import alpha.company.pc.R
import alpha.company.pc.data.models.local.SellerType
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.databinding.ActivityUserInfoModifyBinding
import alpha.company.pc.ui.viewmodels.UserInfoModifyModel
import alpha.company.pc.utils.OnDialogClicked
import alpha.company.pc.utils.makeDialog
import alpha.company.pc.utils.toast
import com.google.android.material.textfield.MaterialAutoCompleteTextView

private const val TAG = "UserInfoModifyActivity"
private const val ERROR_GET_USER = "Erreur innatendue"
private const val ERROR_SET_USER = "Erreur de La modification des informations d'utilisateur"
private const val SUCCESS_SET_USER = "Informations modifiées avec success"

class UserInfoModifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoModifyBinding
    private lateinit var viewModel: UserInfoModifyModel
    private lateinit var userToModifyId: String

    override fun onCreate(savedInstanceState: Bundle?) {

        //hiding the action bar
        supportActionBar?.hide()

        binding = ActivityUserInfoModifyBinding.inflate(layoutInflater)
        userToModifyId = intent.getStringExtra("id")!!
        viewModel = UserInfoModifyModel(
            UserInfoRepository(
                RetrofitService.getInstance()
            )
        )

        super.onCreate(savedInstanceState)

        binding.apply {
            viewModel.apply {

                getUserById(userToModifyId).observe(this@UserInfoModifyActivity) { oldUser ->

                    isTurning.observe(this@UserInfoModifyActivity) { isTurning ->
                        userModifyProgressBar.isVisible = isTurning
                    }

                    if (oldUser == null) {
                        doOnFail(ERROR_GET_USER)
                    } else {
                        Log.i(TAG, "user retrieved : $oldUser")

                        nameEditText.setText(oldUser.name)
                        phoneEditText.setText(oldUser.phoneNumber)
                        emailEditText.setText(oldUser.email)
                        cityEditText.setText(oldUser.city)
                        setUpTheTypeEditText(getType(oldUser.role.toString()))
                        organisationNameEditText.setText(oldUser.brand)

                        validateTheData()

                        submitChanges.setOnClickListener {

                            val dialog = makeDialog(
                                this@UserInfoModifyActivity,
                                object : OnDialogClicked {
                                    override fun onPositiveButtonClicked() {
                                        val newUser = User(
                                            name = nameEditText.text.toString(),
                                            phoneNumber = phoneEditText.text.toString(),
                                            email = emailEditText.text.toString(),
                                            city = cityEditText.text.toString(),
                                            role = getTypeRequestBody(userTypeEditText.text.toString()),
                                            brand = organisationNameEditText.text.toString(),
                                            password = oldUser.password,
                                            imageUrl = oldUser.imageUrl,
                                            token = oldUser.token
                                        )

                                        updateUser(userToModifyId, newUser)
                                            .observe(this@UserInfoModifyActivity) { annonceModified ->

                                                isTurning.observe(this@UserInfoModifyActivity) {
                                                    userModifyProgressBar.isVisible = it
                                                }

                                                //on annonce modification fail
                                                if (!annonceModified) {
                                                    doOnFail(ERROR_SET_USER)
                                                } else {
                                                    doOnSuccess(SUCCESS_SET_USER)
                                                }
                                            }

                                    }

                                    override fun onNegativeButtonClicked() {
                                        //doNothing
                                    }
                                },
                                getString(R.string.user_modify_dialog_title),
                                getString(R.string.user_modify_dialog_message)

                            )
                            dialog.show()
                        }
                    }
                }

                isValidInput.observe(this@UserInfoModifyActivity) {
                    submitChanges.isEnabled = it
                }

            }
        }

        setContentView(binding.root)
    }

    private fun doOnSuccess(message: String) {
        this.toast(message, Toast.LENGTH_SHORT)
        finish()
    }

    private fun doOnFail(message: String) {
        this.toast(message, Toast.LENGTH_SHORT)
        geToMainActivity()
    }

    private fun geToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setUpTheTypeEditText(default: String?) {

        binding.userTypeEditText.setText(default)

        //to change !!!!!!!!!!!!??
        //set the adapter
        val values = SellerType.values().map { sellerType ->
            getType(sellerType.type)
        }

        val adapter = ArrayAdapter(this, R.layout.list_item, values)
        (binding.userTypeTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun getType(valueInEnglish: String): String {
        //to change with translation functionality
        return when (valueInEnglish) {
            SellerType.SOLO.type -> "Utilisteur normal"
            SellerType.PRO.type -> "Utilisteur professionnel"
            else -> {
                "erreur"
            }
        }
    }

    private fun getTypeRequestBody(valueTranslated: String): String {
        //to change with translation functionality
        return when (valueTranslated) {
            "Utilisteur normal" -> SellerType.SOLO.type
            "Utilisteur professionnel" -> SellerType.PRO.type
            else -> {
                SellerType.SOLO.type
            }
        }
    }

    private fun validateTheData() {

        binding.apply {

            submitChanges.isEnabled = true

            viewModel.apply {
                initialiseLiveData(
                    nameEditText.text.toString(),
                    phoneEditText.text.toString(),
                    emailEditText.text.toString()
                )
            }

            nameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    nameLiveData.value = text.toString()
                    nameHelperText.observe(this@UserInfoModifyActivity) {
                        nameTextField.helperText = it
                    }
                }
            }

            phoneEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    phoneLiveData.value = text.toString()
                    phoneHelperText.observe(this@UserInfoModifyActivity) {
                        phoneTextField.helperText = it
                    }
                }
            }

            emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    emailLiveData.value = text.toString()
                    emailHelperText.observe(this@UserInfoModifyActivity) {
                        emailTextField.helperText = it
                    }
                }
            }
        }

    }
}