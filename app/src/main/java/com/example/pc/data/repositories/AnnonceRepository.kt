package com.example.pc.data.repositories

import com.example.pc.data.models.network.NewFavouritesRequest
import com.example.pc.data.remote.RetrofitService

class AnnonceRepository(private val retrofitService: RetrofitService) {
    fun getAnnonceById(annonceId: String) = retrofitService.getAnnonceById(annonceId)
    fun getUserById(userId: String) = retrofitService.getUserById(userId)
    fun addToFavourites(userId: String, newAnnonceList: NewFavouritesRequest) =
        retrofitService.updateFavourites(userId, newAnnonceList)

    fun getUserAnnonces(userId: String) = retrofitService.getAnnounces(userId)
}