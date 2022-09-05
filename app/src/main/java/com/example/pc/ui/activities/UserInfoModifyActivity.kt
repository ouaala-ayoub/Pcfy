package com.example.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.example.pc.R
import com.example.pc.data.models.local.SellerType
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.ActivityUserInfoModifyBinding
import com.example.pc.ui.viewmodels.UserInfoModifyModel
import com.example.pc.utils.toast
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

                getUserById(userToModifyId).observe(this@UserInfoModifyActivity){ oldUser ->

                    isTurning.observe(this@UserInfoModifyActivity){ isTurning ->
                        userModifyProgressBar.isVisible = isTurning
                    }

                    if (oldUser == null) {
                        doOnFail(ERROR_GET_USER)
                    }
                    else {
                        Log.i(TAG, "user retrieved : $oldUser")



                    }
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

    private fun setUpTheTypeEditText(default: String){

        binding.userTypeEditText.setText(default)

        //to change !!!!!!!!!!!!??
        //set the adapter
        val values = SellerType.values().map {
                sellerType -> sellerType.type
        }

        val adapter = ArrayAdapter(this, R.layout.list_item, values)
        (binding.userTypeTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun validateTheData(){

        binding.apply {

            submitChanges.isEnabled = true

            nameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    nameLiveData.value = text.toString()
                    nameHelperText.observe(this@UserInfoModifyActivity){
                        nameTextField.helperText = it
                    }
                }
            }

            phoneEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    phoneLiveData.value = text.toString()
                    phoneHelperText.observe(this@UserInfoModifyActivity){
                        phoneTextField.helperText = it
                    }
                }
            }

            emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    emailLiveData.value = text.toString()
                    emailHelperText.observe(this@UserInfoModifyActivity){
                        emailTextField.helperText = it
                    }
                }
            }

            passwordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    passwordLiveData.value = text.toString()
                    passwordHelperText.observe(this@UserInfoModifyActivity){
                        passwordTextField.helperText = it
                    }
                }
            }

            retypePasswordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    retypedPasswordLiveData.value = text.toString()
                    retypedPasswordHelperText.observe(this@UserInfoModifyActivity){
                        retypePasswordTextField.helperText = it
                    }
                }
            }
        }

    }
}