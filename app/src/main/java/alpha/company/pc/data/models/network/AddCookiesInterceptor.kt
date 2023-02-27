package alpha.company.pc.data.models.network

import alpha.company.pc.utils.LocalStorage
import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

private const val TAG = "AddCookiesInterceptor"

class AddCookiesInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
//        getting tokens tokens
        val tokens = LocalStorage.getTokens(context)
        if (tokens.accessToken != null && tokens.refreshToken != null) {
            Log.d(
                TAG,
                "Cookie jwt-refresh=${tokens.refreshToken}; jwt-access=${tokens.accessToken}"
            )
            builder.addHeader(
                "Cookie",
                "jwt-refresh=${tokens.refreshToken}; jwt-access=${tokens.accessToken}"
            )
        }
        return chain.proceed(builder.build())
    }
}