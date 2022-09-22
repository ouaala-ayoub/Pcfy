package com.example.pc.ui.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val PASS_MIN_LENGTH = 8
private const val EMAIL_PASS_WRONG = "Email ou Mot de passe incorrect"
private const val TAG = "LoginModel"

class LoginModel(private val repository: LoginRepository) : ViewModel() {

    val retrievedTokens = MutableLiveData<Boolean>()
    private val tokens = MutableLiveData<Tokens>()
    val isTurning = MutableLiveData<Boolean>()

    val userNameLiveData = MutableLiveData<String>()
    val passwordLiveData = MutableLiveData<String>()
    private val errorMessage = MutableLiveData("")
    val isValidLiveData = MediatorLiveData<Boolean>().apply {
        addSource(userNameLiveData) { username ->
            val password = passwordLiveData.value
            this.value = validateData(username, password)
        }
        addSource(passwordLiveData) { password ->
            val email = userNameLiveData.value
            this.value = validateData(email, password)
        }
    }

    private fun validateData(email: String?, password: String?): Boolean {
        val isValidEmail = !email.isNullOrBlank() && email.contains("@")
        val isValidPassword = !password.isNullOrBlank() && password.length >= PASS_MIN_LENGTH
        return isValidEmail && isValidPassword
    }

    fun login(userName: String, password: String, activity: Activity): MutableLiveData<Tokens> {

        isTurning.postValue(true)

        repository.login(userName, password).enqueue(object : Callback<Tokens> {
            override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse login : ${response.body()}")
                    retrievedTokens.postValue(true)
                    tokens.postValue(response.body())

                    repository.setCurrentTokens(response.body()!!)

                    Log.i(TAG, "current token: ${LocalStorage.getTokens(activity)}")
                    isTurning.postValue(false)
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse test get error message $error")
                    errorMessage.postValue(error?.message)
                    retrievedTokens.postValue(false)
                    isTurning.postValue(false)
                }
            }

            override fun onFailure(call: Call<Tokens>, t: Throwable) {
                Log.e(TAG, "onFailure login : ${t.message}")
                errorMessage.postValue(t.message)
                retrievedTokens.postValue(false)
                isTurning.postValue(false)
            }
        })

        return tokens
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

}