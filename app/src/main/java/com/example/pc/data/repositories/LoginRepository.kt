package com.example.pc.data.repositories

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pc.R
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.models.network.UserCredentials
import com.example.pc.data.remote.RetrofitService
import com.example.pc.utils.Auth
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.Token
import retrofit2.Call


@RequiresApi(Build.VERSION_CODES.O)
class LoginRepository(private val retrofitService: RetrofitService, activity: Activity) {

    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    fun logout() {
        user = null
        retrofitService.logout()
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser?) {
        this.user = loggedInUser
    }

    init {
        if (isAuthenticated(activity)) {
            setLoggedInUser(
                LoggedInUser(
                    Token.getUserId(activity)!!
                )
            )
        }
        else setLoggedInUser(null)
    }

    fun setCurrentTokens(activity: Activity, token: Tokens){
        LocalStorage.storeTokens(activity, token)
    }

    private fun isAuthenticated(activity: Activity) = Auth.isAuthenticated(activity)

    fun login(userName: String, password: String): Call<Tokens> {
        return retrofitService.login(UserCredentials(userName, password))
    }
}