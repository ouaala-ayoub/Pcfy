package com.example.pc.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pc.JWT_USER_ACCESS
import com.example.pc.JWT_USER_REFRESH
import com.example.pc.data.models.network.AccessToken
import com.example.pc.data.models.network.RefreshToken
import com.example.pc.data.remote.RetrofitService
import io.github.nefilim.kjwt.*
import io.github.nefilim.kjwt.ClaimsVerification.expired
import io.github.nefilim.kjwt.ClaimsVerification.validateClaims
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "Token"

class Token {
    companion object {

        private val errorMessage = MutableLiveData<String>()
        val newAccessToken = MutableLiveData<String>()

        fun accessTokenIsValid(activity: Context): Boolean{
            //get the token from local storage
            val accessToken = LocalStorage.getAccessToken(activity) ?: return false
            Log.i(TAG, "accessTokenIsValid : access Token Retrieved ")

            //decode the token
            val decodedAccessToken = JWT.decodeT(accessToken, JWSHMAC256Algorithm).orNull() ?: return false
            Log.i(TAG, "accessTokenIsValid : access Token Decoded $decodedAccessToken")
            //return isValid
            val isValid = isTokenValid(decodedAccessToken, JWT_USER_ACCESS)
            Log.i(TAG, "accessTokenIsValid : access Token Is Valid: $isValid")
            return isValid
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun createAccessToken(context: Context): LiveData<String?>? {

            //to change
            val retrofitService = RetrofitService.getInstance()
            val refreshToken = LocalStorage.getRefreshToken(context) ?: return null

            retrofitService.getAccessToken(RefreshToken(refreshToken)).enqueue(object: Callback<AccessToken>{
                override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                    Log.i(TAG, "onResponse: ${response.body()?.accessToken}")
                    newAccessToken.postValue(response.body()?.accessToken)
                }

                override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                }

            })
            return newAccessToken
        }


        private fun isTokenValid(token: DecodedJWT<JWSHMAC256Algorithm>, secretKey: String): Boolean {
            val isValid: Boolean
            var verificationRes: JWT<JWSHMAC256Algorithm>? = null
            verifySignature(token, secretKey).fold(
                {
                    errorMessage.postValue(it.toString())
                    Log.e(TAG, "isTokenValid error : ${errorMessage.value}", )
                },
                {
                    verificationRes = it
                }
            )
            Log.i(TAG, "isTokenValid verificationRes : $verificationRes")
            isValid = verificationRes != null
            return isValid
        }

        private fun isExpired(token: DecodedJWT<JWSHMAC256Algorithm>, secretKey: String): Boolean {
            val standardValidation: ClaimsValidator = { claims ->
                validateClaims(
                    expired,
                )(claims)
            }
            val verificationRes = verify(token, secretKey, standardValidation)
            Log.i(TAG, "isExpired: token isExpired: ${!verificationRes.isValid}")
            return !verificationRes.isValid
        }

        fun accessTokenIsExpired(activity: Context): Boolean{
            //get the token from local storage
            val accessToken = LocalStorage.getAccessToken(activity) ?: return false
            Log.i(TAG, "accessTokenIsExpired access Token Retrieved")

            //decode the token
            val decodedAccessToken = JWT.decodeT(accessToken, JWSHMAC256Algorithm).orNull() ?: return false
            Log.i(TAG, "accessTokenIsExpired access Token Decoded")

            //return isExpired
            val isExpired = isExpired(decodedAccessToken, JWT_USER_ACCESS)
            Log.i(TAG, "accessTokenIsExpired access Token Is Expired: $isExpired")
            return isExpired
        }

        fun getUserId(activity: Context): String?{

            val refreshToken = LocalStorage.getRefreshToken(activity)
            Log.i(TAG, "refresh Token Retrieved to get userId $refreshToken")

            if(refreshToken != null){
                Log.i(TAG, "getUserId refresh token $refreshToken ")

                JWT.decode(refreshToken).fold(
                    {
                        errorMessage.postValue(it.toString())
                        Log.e(TAG, "getUserId decoding token error : ${errorMessage.value}" )
                    },
                    {
                        val userId = it.claimValue("id").orNull()
                        Log.i(TAG, "getUserId: current returned $userId")
                        return userId
                    }
                )
            }

            Log.i(TAG, "getUserId: returned null")
            return null
        }

    }
}