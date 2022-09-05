package com.example.pc.data.repositories

import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService

class UserInfoRepository(private val retrofitService: RetrofitService) {
    fun getUserById(userId: String) = retrofitService.getUserById(userId)
    fun getAnnonces(userId: String) = retrofitService.getAnnounces(userId)
    fun deleteAnnonce(annonceId: String) = retrofitService.deleteAnnonce(annonceId)
    fun updateUserInfo(userId: String, newUser: User) = retrofitService.updateUserInfo(userId, newUser)
}