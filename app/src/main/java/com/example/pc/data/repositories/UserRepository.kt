package com.example.pc.data.repositories

import com.example.pc.data.models.local.TokenRequest
import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import okhttp3.RequestBody
import retrofit2.Call

class UserRepository(private val retrofitService: RetrofitService) {

    fun addUser(userToAdd: RequestBody): Call<IdResponse> = retrofitService.addUser(userToAdd)

    fun registerToken(userId: String, token: String) =
        retrofitService.putFireBaseToken(userId, TokenRequest(token))
}