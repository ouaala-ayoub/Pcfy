package alpha.company.pc.data.remote

import alpha.company.pc.data.models.local.MessageResponse
import alpha.company.pc.data.models.network.Message
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RetrofitNotificationService {

//    @Headers(
//        "Content-Type:application/json",
//        "Authorization:key=$SERVER_KEY"
//    )
    @POST("send")
    fun sendMessage(
        @Body message: Message,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") fireBaseKey: String ,
    ): Call<MessageResponse>

    companion object {

        //to learn
        private const val BASE_URL = "https://fcm.googleapis.com/fcm/"
        private var retrofitService: RetrofitNotificationService? = null

        fun getInstance(): RetrofitNotificationService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitNotificationService::class.java)
            }
            return retrofitService!!
        }
    }
}
