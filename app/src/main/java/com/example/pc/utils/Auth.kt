package com.example.pc.utils

import android.content.Context
import android.util.Log
import com.example.pc.data.models.network.AuthBody
import com.example.pc.data.models.network.BodyX
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import kotlinx.coroutines.coroutineScope
import retrofit2.await

private const val TAG = "Auth"

class Auth(val loginRepository: LoginRepository) {
    companion object {

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