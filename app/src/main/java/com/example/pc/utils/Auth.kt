package com.example.pc.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.pc.data.repositories.LoginRepository

private const val TAG = "Auth"

class Auth(val loginRepository: LoginRepository) {
    companion object {

        @RequiresApi(Build.VERSION_CODES.O)
        fun isAuthenticated(
            context: Context,
        ): Boolean{

            try {
                Token.apply {
                    LocalStorage.apply {
                        if (!accessTokenIsValid(context)) return false
                        return if (!accessTokenIsExpired(context)) true
                        else {
                            val newAccessToken = createAccessToken(context)?.value ?: return false
                            Log.i(TAG, "isAuthenticated new access token: $newAccessToken")
                            storeAccessToken(context, newAccessToken)
                            true
                        }
                    }
                }
            }
            catch (e: Error){
                Log.e(TAG, "isAuthenticated: ${e.message} " )
                return false
            }
        }
    }
}