package com.example.pc.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pc.R
import com.example.pc.data.models.network.Tokens
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.JWTKeyID
import io.github.nefilim.kjwt.sign
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class LocalStorage {

    companion object {

        fun storeTokens(activity: Activity, token: Tokens){
            val sharedPrefs = activity.getPreferences(Context.MODE_PRIVATE)
            with (sharedPrefs!!.edit()) {
                putString(activity.applicationContext.getString(R.string.refresh_token), token.refreshToken)
                putString(activity.applicationContext.getString(R.string.access_token), token.accessToken)
                apply()
            }
        }

        fun storeRefreshToken(activity: Activity, refreshToken: String){
            val sharedPrefs = activity.getPreferences(Context.MODE_PRIVATE)
            with (sharedPrefs!!.edit()) {
                putString(activity.applicationContext.getString(R.string.refresh_token), refreshToken)
                apply()
            }
        }

        fun storeAccessToken(activity: Activity, accessToken: String){
            val sharedPrefs = activity.getPreferences(Context.MODE_PRIVATE)
            with (sharedPrefs!!.edit()) {
                putString(activity.applicationContext.getString(R.string.access_token), accessToken)
                apply()
            }
        }

        fun getTokens(activity: Activity): Tokens {
            return Tokens(
                getAccessToken(activity),
                getRefreshToken(activity)
            )
        }

        private fun getAccessToken(activity: Activity): String?{
            val accessToken: String?
            activity.apply {
                accessToken = getPreferences(Context.MODE_PRIVATE)
                    .getString(
                        applicationContext.getString(R.string.access_token), "not_found"
                    )
            }
            return accessToken
        }

        private fun getRefreshToken(activity: Activity): String?{
            val accessToken: String?
            activity.apply {
                accessToken = getPreferences(Context.MODE_PRIVATE)
                    .getString(
                        applicationContext.getString(R.string.refresh_token), "not_found"
                    )
            }
            return accessToken
        }

    }
}


