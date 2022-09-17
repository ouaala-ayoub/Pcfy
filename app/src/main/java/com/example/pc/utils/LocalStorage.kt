package com.example.pc.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.pc.R
import com.example.pc.data.models.network.Tokens

private const val TAG = "LocalStorage"

class LocalStorage {

    companion object {

        fun storeTokens(activity: Context, token: Tokens){
            val sharedPrefs = activity.getSharedPreferences("tokens",Context.MODE_PRIVATE)
            with (sharedPrefs!!.edit()) {
                putString(activity.resources.getString(R.string.refresh_token), token.refreshToken)
                putString(activity.resources.getString(R.string.access_token), token.accessToken)
                apply()
            }
            Log.i(TAG, "stored Tokens: ${getTokens(activity)}")

        }

        fun storeAccessToken(activity: Context, accessToken: String){
            val sharedPrefs = activity.getSharedPreferences("tokens", Context.MODE_PRIVATE)
            with (sharedPrefs!!.edit()) {
                putString(activity.applicationContext.getString(R.string.access_token), accessToken)
                apply()
            }
        }

        fun getTokens(activity: Context): Tokens {
            return Tokens(
                getRefreshToken(activity),
                getAccessToken(activity)
            )
        }

        fun getAccessToken(activity: Context): String?{
            val accessToken: String?
            activity.apply {
                accessToken = getSharedPreferences("tokens",Context.MODE_PRIVATE)
                    .getString(
                        resources.getString(R.string.access_token), null
                    )
            }
            return accessToken
        }

        fun getRefreshToken(activity: Context): String?{
            val refreshToken: String?
            activity.apply {
                refreshToken = getSharedPreferences("tokens", Context.MODE_PRIVATE)
                    .getString(
                        resources.getString(R.string.refresh_token), null
                    )
            }
            return refreshToken
        }

        fun deleteTokens(activity: Context) : Boolean{
            try {
                val sharedPrefs = activity.getSharedPreferences("tokens", Context.MODE_PRIVATE)
                val editor = sharedPrefs.edit()
                editor
                    .remove(activity.resources.getString(R.string.refresh_token))
                    .remove(activity.resources.getString(R.string.access_token))
                    .apply()
            }
            catch (e: Throwable){
                return false
            }
            return true
        }

        fun isDarkTheme(context: Context){
//            context.getSharedPreferences("")
        }

    }
}


