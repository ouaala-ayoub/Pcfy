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
        val accessToken = LocalStorage.getAccessToken(context)
        if (accessToken != null) {
            Log.d(TAG, "sending authorization header  jwt-access=${accessToken}")
            builder.addHeader(
                "authorization",
                "Bearer $accessToken"
            )
        }
        return chain.proceed(builder.build())
    }
}