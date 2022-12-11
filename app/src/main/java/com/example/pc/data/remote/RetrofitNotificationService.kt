package com.example.pc.data.remote

import com.example.pc.data.models.local.MessageResponse
import com.example.pc.data.models.network.Message
import com.example.pc.utils.SERVER_KEY
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitNotificationService {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=$SERVER_KEY"
    )
    @POST("send")
    fun sendMessage(@Body message: Message): Call<MessageResponse>

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
