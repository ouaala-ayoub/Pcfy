package com.example.pc.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.repositories.LoginRepository

private const val PASS_MIN_LENGTH = 8

class LoginModel(private val repository: LoginRepository) : ViewModel() {

    //to add repository

    private val loginSuccessful = MutableLiveData<Boolean>()

    val userNameLiveData = MutableLiveData<String>()
    val passwordLiveData = MutableLiveData<String>()
    val isValidLiveData = MediatorLiveData<Boolean>().apply {
        addSource(userNameLiveData){ username->
            val password = passwordLiveData.value
            this.value = validateData(username, password)
        }
        addSource(passwordLiveData){ password->
            val email = userNameLiveData.value
            this.value = validateData(email, password)
        }
    }

    private fun validateData(email: String?, password: String?): Boolean {
        val isValidEmail = !email.isNullOrBlank() && email.contains("@")
        val isValidPassword = !password.isNullOrBlank() && password.length>=PASS_MIN_LENGTH
        return isValidEmail && isValidPassword
    }

    fun login(userName: String, password: String): MutableLiveData<Boolean>{
        val isLoginSuccessful = false

        repository.login(userName, password)

        return loginSuccessful
    }
}