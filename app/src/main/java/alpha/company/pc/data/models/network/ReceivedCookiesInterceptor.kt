package alpha.company.pc.data.models.network

import alpha.company.pc.utils.LocalStorage
import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

private const val TAG = "SetCookiesInterceptor"

class ReceivedCookiesInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        Log.i(TAG, "intercept: ${originalResponse.headers("set-cookie")}")
        if (originalResponse.headers("set-cookie").isNotEmpty()) {

            val cookies = originalResponse.headers("set-cookie")
            Log.d(TAG, "intercept cookies 0 : $cookies")
            if (cookies.size == 2) {
                val accessToken = cookies[0].split(";")[0].split("=")[1]
                val refreshToken = cookies[1].split(";")[0].split("=")[1]
                Log.d(TAG, "access token intercepted : $accessToken")
                Log.d(TAG, "refresh token intercepted : $refreshToken")
                val tokens = Tokens(
                    refreshToken,
                    accessToken
                )
                LocalStorage.storeTokens(context, tokens)
            } else if (cookies.size == 1) {
                val accessToken = cookies[0].split(";")[0].split("=")[1]
                Log.d(TAG, "access token intercepted : ${cookies[0].split(";")[0].split("=")[1]}")
                LocalStorage.storeAccessToken(context, accessToken)
            }
        }
        return originalResponse
    }
}