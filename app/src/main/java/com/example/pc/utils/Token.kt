package com.example.pc.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import arrow.core.*
import com.example.pc.JWT_USER_ACCESS
import com.example.pc.JWT_USER_REFRESH
import io.github.nefilim.kjwt.*
import io.github.nefilim.kjwt.ClaimsVerification.expired
import io.github.nefilim.kjwt.ClaimsVerification.validateClaims
import okio.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private const val TAG = "Token"

class Token {
    companion object {

        private val errorMessage = MutableLiveData<String>()

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

        fun refreshTokenIsValid(activity: Context): Boolean {
            //get the token from local storage
            val refreshToken = LocalStorage.getRefreshToken(activity) ?: return false
            Log.i(TAG, "refreshTokenIsValid : refresh Token Retrieved")

            //decode the token
            val decodedRefreshToken = JWT.decodeT(refreshToken, JWSHMAC256Algorithm).orNull() ?: return false
            Log.i(TAG, "refreshTokenIsValid : decoded Refresh Token : $decodedRefreshToken")

            //return isValid
            val isValid = isTokenValid(decodedRefreshToken, JWT_USER_REFRESH)
            Log.i(TAG, "refreshTokenIsValid : refresh Token Is Valid: $isValid")
            return isValid
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun createAccessToken(userId: String): String? {

            val jwt = JWT.hs256 {
                claim("id", userId)
                issuedNow()
                expiresAt(
                    LocalDateTime.ofInstant(
                        Date(System.currentTimeMillis()  + 900).toInstant(),
                        ZoneId.of("UTC")
                    )
                )
            }

            var jwtToken: String? = null
            jwt.sign(JWT_USER_ACCESS).fold(
                {
                    errorMessage.postValue(it.toString())
                    Log.e(TAG, "createAccessToken error : ${errorMessage.value}" )
                },
                {
                    jwtToken = it.rendered
                }
            )

            Log.i(TAG, "createAccessToken jwt signed : $jwtToken")
            return jwtToken
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

        fun refreshTokenIsExpired(activity: Context): Boolean {
            //get the token from local storage
            val refreshToken = LocalStorage.getRefreshToken(activity) ?: return false
            Log.i(TAG, "refreshTokenIsExpired refresh Token Retrieved")
            
            //decode the token
            val decodedRefreshToken = JWT.decodeT(refreshToken, JWSHMAC256Algorithm).orNull() ?: return false
            Log.i(TAG, "refreshTokenIsExpired decoded Refresh Token")
            
            //return isValid
            val isExpired = isExpired(decodedRefreshToken, JWT_USER_REFRESH)
            Log.i(TAG, "refreshTokenIsExpired refresh Token Is Expired: $isExpired")
            return isExpired
        }

        @RequiresApi(Build.VERSION_CODES.O)
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