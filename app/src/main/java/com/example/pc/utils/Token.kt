package com.example.pc.utils

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import arrow.core.Either
import com.example.pc.JWT_USER_ACCESS
import com.example.pc.JWT_USER_REFRESH
import com.example.pc.data.models.network.Tokens
import io.github.nefilim.kjwt.*
import io.github.nefilim.kjwt.ClaimsVerification.expired
import io.github.nefilim.kjwt.ClaimsVerification.validateClaims
import java.security.interfaces.RSAPublicKey
import java.time.Instant
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
            return verifyToken(decodedAccessToken, JWT_USER_ACCESS).isValid
        }

        fun refreshTokenIsValid(activity: Activity): Boolean{

            //get the token from local storage
            val refreshToken = LocalStorage.getRefreshToken(activity) ?: return false

            //decode the token
            val decodedRefreshToken = JWT.decodeT(refreshToken, JWSHMAC256Algorithm).orNull() ?: return false

            //return isValid
            return verifyToken(decodedRefreshToken, JWT_USER_REFRESH).isValid
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun createAccessToken(userId: String): String? {

            val jwt = JWT.hs256 {
                claim("id", userId)
                issuedAt(
                    LocalDateTime.ofInstant(
                        Date(System.currentTimeMillis() ).toInstant(),
                        ZoneId.of("UTC")
                    )
                )
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

//        @RequiresApi(Build.VERSION_CODES.O)
//        fun createRefreshToken(userId: String): String? {
//
//            val jwt = JWT.hs256 {
//                claim("id", userId)
//                issuedAt(
//                    LocalDateTime.ofInstant(
//                        Date(System.currentTimeMillis() ).toInstant(),
//                        ZoneId.of("UTC")
//                    )
//                )
//                expiresAt(
//                    LocalDateTime.ofInstant(
//                        Date(System.currentTimeMillis()  + 604800).toInstant(),
//                        ZoneId.of("UTC")
//                    )
//                )
//            }
//
//            var jwtToken: String? = null
//            jwt.sign(JWT_USER_REFRESH).tap {
//                jwtToken = it.rendered
//            }
//            return jwtToken
//        }

        private fun verifyToken(token: DecodedJWT<JWSHMAC256Algorithm>, secretKey: String): ClaimsValidatorResult {
            val standardValidation: ClaimsValidator = { claims ->
                validateClaims(
                    expired,
                )(claims)
            }
            return verify(token, secretKey, standardValidation)
        }

        fun getUserId(activity: Activity): String?{
            val accessToken = LocalStorage.getAccessToken(activity)
            if(accessToken != null){
                JWT.decode(accessToken).tap {
                    return it.claimValue("id").orNull()
                }
            }
            return null
        }

    }
}