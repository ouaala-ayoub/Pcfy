package com.example.pc.data.repositories

import android.app.Activity
import android.content.Context
import com.example.pc.R
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.models.network.UserCredentials
import com.example.pc.data.remote.RetrofitService




class LoginRepository(private val retrofitService: RetrofitService) {
//     in-memory cache of the loggedInUser object
//    var user: LoggedInUser? = null
//        private set
//
//    val isLoggedIn: Boolean
//        get() = user != null
//
//    init {
//        // If user credentials will be cached in local storage, it is recommended it be encrypted
//        // @see https://developer.android.com/training/articles/keystore
//        user = null
//    }
//
//    fun logout() {
//        user = null
//        retrofitService.logout()
//    }
//
//    fun login(username: String, password: String): Result<LoggedInUser> {
//        // handle login
//        val result = retrofitService.login(username, password)
//
//        if (result is Result.Success) {
//            setLoggedInUser(result.data)
//        }
//
//        return result
//    }
//
//    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
//        this.user = loggedInUser
//        // If user credentials will be cached in local storage, it is recommended it be encrypted
//        // @see https://developer.android.com/training/articles/keystore
//    }

    init {

    }

    fun login(userName: String, password: String) = retrofitService.login(UserCredentials(userName, password))
}