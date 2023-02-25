package alpha.company.pc.utils

import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.*

private const val TAG = "PayloadClass"

data class PayloadClass(
    @SerializedName("id") var id: String? = null,
    @SerializedName("iat") var iat: Int? = null,
    @SerializedName("exp") var exp: Int? = null
) {
    companion object {
        fun getInfoFromJwt(jwt: String): PayloadClass? {
            try {
                val payload = jwt.split(".")[1]

                val decodedPayload = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i(TAG, "Build version > 26")
                    String(Base64.getDecoder().decode(payload))
                } else {
                    Log.i(TAG, "Build version < 26")
                    android.util.Base64.decode(payload.toByteArray(), android.util.Base64.DEFAULT)
                        .toString(charset("UTF-8"))
                }
                Log.d(TAG, "decodedPayload: $decodedPayload")
                val gson = Gson().fromJson(decodedPayload, PayloadClass::class.java)
                Log.d(TAG, "getInfoFromJwt: $gson")
                return gson

            } catch (e: Exception) {
                for (element in e.stackTrace) {
                    Log.e(TAG, "$element")
                }
                return null
            }
        }
    }

    fun isExpired(): Boolean {
        val currentTime = System.currentTimeMillis() / 100
        println("current time = $currentTime")
        return if (exp != null) {
            currentTime > exp!!
        } else {
            false
        }
    }
}
