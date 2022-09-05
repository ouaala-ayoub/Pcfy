package com.example.pc.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.User
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserInfoModifyModel"

class UserInfoModifyModel(private val userInfoRepository: UserInfoRepository): ViewModel() {

    private val oldUser = MutableLiveData<User>()
    private val updatedUser = MutableLiveData<Boolean>()
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


    fun updateUser(userId: String, newUser: User): LiveData<Boolean> {

        isTurning.postValue(true)

        userInfoRepository.updateUserInfo(userId, newUser).enqueue(object: Callback<User> {

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful && response.body() != null){
                    updatedUser.postValue(true)
                }
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse error code : ${error?.message}")
                    updatedUser.postValue(false)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure updateUser : ${t.message}")
                updatedUser.postValue(false)
                isTurning.postValue(false)
            }

        })

        return updatedUser
    }

    fun getUserById(userId: String): LiveData<User> {

        isTurning.postValue(true)

        userInfoRepository.getUserById(userId).enqueue(object: Callback<User>{

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null){
                    oldUser.postValue(response.body())
                }
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "error body : $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                isTurning.postValue(false)
            }
        })

        return oldUser
    }
}