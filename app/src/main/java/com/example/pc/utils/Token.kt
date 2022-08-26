package com.example.pc.utils

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
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

class Token {
    companion object {

        fun accessTokenIsValid(activity: Activity): Boolean{
            //get the token from local storage
            val accessToken = LocalStorage.getAccessToken(activity) ?: return false
            //decode the token
            val decodedAccessToken = JWT.decodeT(accessToken, JWSHMAC256Algorithm).orNull() ?: return false
            //return isValid
            return isTokenValid(decodedAccessToken, JWT_USER_ACCESS)
        }

        fun refreshTokenIsValid(activity: Activity): Boolean {
            //get the token from local storage
            val refreshToken = LocalStorage.getAccessToken(activity) ?: return false
            //decode the token
            val decodedRefreshToken = JWT.decodeT(refreshToken, JWSHMAC256Algorithm).orNull() ?: return false
            //return isValid
            return isTokenValid(decodedRefreshToken, JWT_USER_REFRESH)
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
            jwt.sign(JWT_USER_REFRESH).tap {
                jwtToken = it.rendered
            }
            return jwtToken
        }

        private fun isTokenValid(token: DecodedJWT<JWSHMAC256Algorithm>, secretKey: String): Boolean {
            val isValid: Boolean
            val verificationRes = verifySignature<JWSHMAC256Algorithm>(token, secretKey).orNull()
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
            return !verificationRes.isValid
        }

        fun accessTokenIsExpired(activity: Activity): Boolean{
            //get the token from local storage
            val accessToken = LocalStorage.getAccessToken(activity) ?: return false
            //decode the token
            val decodedAccessToken = JWT.decodeT(accessToken, JWSHMAC256Algorithm).orNull() ?: return false
            //return isValid
            return isExpired(decodedAccessToken, JWT_USER_ACCESS)
        }

        fun refreshTokenIsExpired(activity: Activity): Boolean {
            //get the token from local storage
            val refreshToken = LocalStorage.getAccessToken(activity) ?: return false
            //decode the token
            val decodedRefreshToken = JWT.decodeT(refreshToken, JWSHMAC256Algorithm).orNull() ?: return false
            //return isValid
            return isExpired(decodedRefreshToken, JWT_USER_REFRESH)
        }

        fun getUserId(activity: Activity): String?{
            val refreshToken = LocalStorage.getRefreshToken(activity)
            if(refreshToken != null){
                JWT.decode(refreshToken).tap {
                    return it.claimValue("id").orNull()
                }
            }
            return null
        }

    }
}