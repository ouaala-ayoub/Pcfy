package com.example.pc.utils

import android.app.Activity
import io.github.nefilim.kjwt.JWT

class Auth {
    companion object {
        fun isAuthenticated(activity: Activity): Boolean{
            val tokens = LocalStorage.getTokens(activity)
//            val userRefresh = BuildConfig.
            return true
        }
    }
}