package com.example.pc.data.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.models.network.UserCredentials
import com.example.pc.data.remote.RetrofitService
import com.example.pc.utils.Auth.Companion.isAuthenticated
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.Token
import retrofit2.Call

private const val TAG = "LoginRepository"

class LoginRepository(private val retrofitService: RetrofitService,private val activity: Context) {

    var user: LoggedInUser? = null
    var isLoggedIn = MutableLiveData(isAuthenticated())

    init {
        val checkForAuth = isAuthenticated()
        Log.i(TAG, "init login Repo: $checkForAuth")
        Log.i(TAG, "current tokens : ${LocalStorage.getTokens(activity)}")
        isLoggedIn.postValue(checkForAuth)

        if (checkForAuth) {
            setLoggedInUser(
                Token.getPayload(activity)!!
            )
        }
        else setLoggedInUser(null)
    }

    fun logout() {
        LocalStorage.deleteTokens(activity)
        isLoggedIn.postValue(false)
        user = null
//        retrofitService.logout()
    }

    fun setLoggedInUser(loggedInUser: LoggedInUser?) {
        isLoggedIn.postValue(true)
        user = loggedInUser
    }

    fun setCurrentTokens(token: Tokens){
        LocalStorage.storeTokens(activity, token)
    }

    private fun isAuthenticated(): Boolean {
        return isAuthenticated(activity)
    }

    fun login(userName: String, password: String): Call<Tokens> {
        return retrofitService.login(UserCredentials(userName, password))
    }
}