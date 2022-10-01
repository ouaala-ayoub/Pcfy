package com.example.pc.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.repositories.UserRepository
import com.example.pc.utils.getError
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserModel"
private const val NO_NAME = "Entrez un nom"
private const val TEN_NUMBERS = "Doit etre composé de 10 chiffres"
private const val INVALID_EMAIL = "Email Invalide"
private const val MIN_EIGHT = "Au moins 8 caracteres"
private const val NON_IDENTICAL = "Mot de passes non identiques"

class UserModel(private val repository: UserRepository) : ViewModel() {
    //add business logic
    //sign up = addUser

    private val userAdded = MutableLiveData<String?>()
    val isTurning = MutableLiveData<Boolean>()

    val nameLiveData = MutableLiveData<String>()
    val nameHelperText = MutableLiveData<String>()

    val phoneLiveData = MutableLiveData<String>()
    val phoneHelperText = MutableLiveData<String>()

    val emailLiveData = MutableLiveData<String>()
    val emailHelperText = MutableLiveData<String>()

    val passwordLiveData = MutableLiveData<String>()
    val passwordHelperText = MutableLiveData<String>()

    val retypedPasswordLiveData = MutableLiveData<String>()
    val retypedPasswordHelperText = MutableLiveData<String>()

    val isValidInput = MediatorLiveData<Boolean>().apply {
        addSource(nameLiveData) { name ->
            this.value = validateTheData(
                name,
                phoneLiveData.value,
                emailLiveData.value,
                passwordLiveData.value,
                retypedPasswordLiveData.value
            )
        }
        addSource(phoneLiveData) { phone ->
            this.value = validateTheData(
                nameLiveData.value,
                phone,
                emailLiveData.value,
                passwordLiveData.value,
                retypedPasswordLiveData.value
            )
        }
        addSource(emailLiveData) { email ->
            this.value = validateTheData(
                nameLiveData.value,
                phoneLiveData.value,
                email,
                passwordLiveData.value,
                retypedPasswordLiveData.value
            )
        }
        addSource(passwordLiveData) { password ->
            this.value = validateTheData(
                nameLiveData.value,
                phoneLiveData.value,
                emailLiveData.value,
                password,
                retypedPasswordLiveData.value
            )
        }
        addSource(retypedPasswordLiveData) { retypedPassword ->
            this.value = validateTheData(
                nameLiveData.value,
                phoneLiveData.value,
                emailLiveData.value,
                passwordLiveData.value,
                retypedPassword
            )
        }
    }

    private fun validateTheData(
        name: String?,
        phone: String?,
        email: String?,
        password: String?,
        retypedPassword: String?
    ): Boolean {
        val isValidName = !name.isNullOrBlank()
        val isValidPhone = phone?.length == 10 && phone.matches(".*[0-9].*".toRegex())

        val isValidEmail = if (email.isNullOrBlank()) false
        else
            Patterns.EMAIL_ADDRESS.matcher(
                email
            ).matches()

        val isValidPassword = if (password.isNullOrBlank()) false else password.length >= 8
        val isValidRetypedPassword = retypedPassword == password

        if (!isValidName) {
            nameHelperText.value = NO_NAME
        } else nameHelperText.value = ""

        if (!isValidPhone) {
            phoneHelperText.value = TEN_NUMBERS
        } else phoneHelperText.value = ""

        if (!isValidEmail) {
            emailHelperText.value = INVALID_EMAIL
        } else emailHelperText.value = ""

        if (!isValidPassword) {
            passwordHelperText.value = MIN_EIGHT
        } else passwordHelperText.value = ""

        if (!isValidRetypedPassword) {
            retypedPasswordHelperText.value = NON_IDENTICAL
        } else retypedPasswordHelperText.value = ""

        return isValidName && isValidPhone && isValidEmail && isValidPassword && isValidRetypedPassword
    }

    fun signUp(userToAdd: RequestBody): MutableLiveData<String?> {

        isTurning.postValue(true)

        repository.addUser(userToAdd).enqueue(object : Callback<IdResponse> {

            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {

                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse: response body = ${response.body()}")
                    userAdded.postValue(response.body()!!.objectId)
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.i(TAG, "onResponse: $error")
                    userAdded.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                userAdded.postValue(null)
                isTurning.postValue(false)
                Log.e(TAG, "error message = ${t.message}")
            }
        })
        return userAdded
    }


}
