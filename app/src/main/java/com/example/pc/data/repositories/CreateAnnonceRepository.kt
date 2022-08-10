package com.example.pc.data.repositories

import com.example.pc.data.models.network.NewAnnonceRequest
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import retrofit2.Call

class CreateAnnonceRepository(private val retrofitService: RetrofitService) {
    //add calls
    fun addAnnonce(annonceToAdd: Annonce): Call<IdResponse> = retrofitService.addAnnonce(annonceToAdd)
    fun addAnnonceIdToUser(
        userId: String,
        annonceId: NewAnnonceRequest
    ): Call<User> = retrofitService.updateAnnonces(userId, annonceId)
    fun getUserById(userId: String) = retrofitService.getUserById(userId)
}