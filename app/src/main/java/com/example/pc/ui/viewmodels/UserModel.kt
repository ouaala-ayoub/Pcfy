package com.example.pc.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.models.network.User
import com.example.pc.data.repositories.UserRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserModel"

class UserModel(private val repository: UserRepository): ViewModel() {
    //add business logic
    //sign up = addUser

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
        addSource(nameLiveData){ name ->
            this.value = validateTheData(
                name,
                phoneLiveData.value,
                emailLiveData.value,
                passwordLiveData.value,
                retypedPasswordLiveData.value
            )
        }
        addSource(phoneLiveData){ phone ->
            this.value = validateTheData(
                nameLiveData.value,
                phone,
                emailLiveData.value,
                passwordLiveData.value,
                retypedPasswordLiveData.value
            )
        }
        addSource(emailLiveData){ email ->
            this.value = validateTheData(
                nameLiveData.value,
                phoneLiveData.value,
                email,
                passwordLiveData.value,
                retypedPasswordLiveData.value
            )
        }
        addSource(passwordLiveData){ password ->
            this.value = validateTheData(
                nameLiveData.value,
                phoneLiveData.value,
                emailLiveData.value,
                password,
                retypedPasswordLiveData.value
            )
        }
        addSource(retypedPasswordLiveData){ retypedPassword ->
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
    ): Boolean{
        val isValidName = !name.isNullOrBlank()
        val isValidPhone = phone?.length == 10 && phone.matches(".*[0-9].*".toRegex())

        val isValidEmail = if (email.isNullOrBlank()) false
        else
            Patterns.EMAIL_ADDRESS.matcher(
            email
        ).matches()

        val isValidPassword = if (password.isNullOrBlank()) false else password.length >= 8
        val isValidRetypedPassword = retypedPassword == password

        if (!isValidName){
            nameHelperText.value = "Entrez un nom"
        }else nameHelperText.value = ""

        if(!isValidPhone){
            phoneHelperText.value = "Doit etre composé de 10 chiffres"
        }else phoneHelperText.value = ""

        if (!isValidEmail) {
            emailHelperText.value = "Email Invalide"
        }else emailHelperText.value = ""

        if (!isValidPassword){
            passwordHelperText.value = "Au moins 8 caracteres"
        }else passwordHelperText.value = ""

        if (!isValidRetypedPassword){
            retypedPasswordHelperText.value = "Mot de passes non identiques"
        }else retypedPasswordHelperText.value = ""

        return isValidName && isValidPhone && isValidEmail && isValidPassword && isValidRetypedPassword
    }

//    fun getTheUser(
//        title: String,
//        phone: String,
//        email: String,
//        password: String,
//        city: String,
//        type: String,
//        organizationName: String,
//        imageUrl: String
//    ): User? {
//        return if (!isValidInput.value!!){
//            null
//        } else User(
//            title,
//            phone,
//            email,
//            password,
//            city = city,
//            userType = type,
//            brand = organizationName,
//            imageUrl = imageUrl
//        )
//    }

    fun signUp(userToAdd: User): IdResponse?{

        var responseToReturn: IdResponse? = null

        repository.addUser(userToAdd).enqueue(object : Callback<IdResponse>{

            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse: response body = ${response.body()}")
                    responseToReturn = response.body()
                }
                else{
                    Log.e(TAG, "response error body = ${response.errorBody()}")
                    Log.e(TAG, "response message = " + response.message())
                }
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "error message = ${t.message}")
            }
        })
        return responseToReturn
    }

}
