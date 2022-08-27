package com.example.pc.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.pc.data.repositories.LoginRepository

private const val TAG = "Auth"

class Auth(val loginRepository: LoginRepository) {
    companion object {

        //fix this dogshit code

        @RequiresApi(Build.VERSION_CODES.O)
        fun isAuthenticated(
            context: Context,
        ): Boolean{

            try {
                Token.apply {
                    LocalStorage.apply {
                        if (!accessTokenIsValid(context)) return false
                        if (!accessTokenIsExpired(context)) return true
                        else {
                            if (!refreshTokenIsValid(context)) return false
                            return if(refreshTokenIsExpired(context)) false
                            else {
                                val userId =  getUserId(context)!!
                                val newAccessToken = createAccessToken(userId)
                                if (newAccessToken != null) {
                                    storeAccessToken(context, newAccessToken)
                                }
                                true
                            }
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