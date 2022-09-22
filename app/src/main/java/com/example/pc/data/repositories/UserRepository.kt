package com.example.pc.data.repositories

import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import retrofit2.Call

class UserRepository(private val retrofitService: RetrofitService) {
    fun addUser(userToAdd: User): Call<IdResponse> = retrofitService.addUser(userToAdd)
}