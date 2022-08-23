package com.example.pc.utils

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import arrow.core.Either
import com.example.pc.JWT_USER_ACCESS
import com.example.pc.JWT_USER_REFRESH
import com.example.pc.data.models.network.Tokens
import io.github.nefilim.kjwt.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class Token {
    companion object {

        fun verifyTokens(token: Tokens): Boolean{

            val refreshToken = token.refreshToken
            val accessToken = token.accessToken

            if (refreshToken == null || accessToken == null)
                return false

            return true
        }

        fun isExpired(token: String): Boolean{
            return true
        }

        fun getUserId(activity: Activity): String?{
            val accessToken = LocalStorage.getTokens(activity).accessToken
            if(accessToken != null){
                JWT.decode(accessToken).tap {
                    return it.claimValue("id").orNull()
                }
            }
            return null
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

        @RequiresApi(Build.VERSION_CODES.O)
        fun createRefreshToken(userId: String): String? {

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
                        Date(System.currentTimeMillis()  + 604800).toInstant(),
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

    }
}