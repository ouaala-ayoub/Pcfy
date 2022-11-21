package com.example.pc.data.repositories

import com.example.pc.data.models.network.NewAnnonceRequest
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import okhttp3.RequestBody

class UserInfoRepository(private val retrofitService: RetrofitService) {

    fun getUserById(userId: String) = retrofitService.getUserById(userId)

    fun getAnnonces(userId: String) = retrofitService.getAnnounces(userId)

    fun getAnnonceOrders(annonceId: String) = retrofitService.getAnnonceOrders(annonceId)

    fun deleteAnnonce(tokens: Tokens, annonceId: String) =
        retrofitService.deleteAnnonce(tokens, annonceId)

    fun updateUserInfo(userId: String, newUser: User) =
        retrofitService.updateUserInfo(userId, newUser)

    fun updateUserImage(userId: String, image: RequestBody) =
        retrofitService.updateProfilePicture(userId, image)

    fun deleteUserImage(userId: String, tokens: RequestBody) =
        retrofitService.deleteProfilePicture(userId, tokens)

}