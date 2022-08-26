package com.example.pc.utils

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.pc.data.repositories.LoginRepository
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.KJWTVerificationError
import io.github.nefilim.kjwt.verify

private const val TAG = "Auth"

class Auth(val loginRepository: LoginRepository) {
    companion object {

        @RequiresApi(Build.VERSION_CODES.O)
        fun isAuthenticated(
            activity: Activity,
        ): Boolean{

            try {
                Token.apply {
                    LocalStorage.apply {
                        if (!accessTokenIsValid(activity)) return false
                        if (!accessTokenIsExpired(activity)) return true
                        else {
                            if (refreshTokenIsValid(activity)) return false
                            return if(refreshTokenIsExpired(activity)) false
                            else {
                                val newAccessToken = createAccessToken(getUserId(activity)!!)
                                if (newAccessToken != null) {
                                    storeAccessToken(activity, newAccessToken)
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