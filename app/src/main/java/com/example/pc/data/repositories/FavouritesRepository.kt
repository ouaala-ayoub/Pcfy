package com.example.pc.data.repositories

import com.example.pc.data.models.network.NewFavouritesRequest
import com.example.pc.data.remote.RetrofitService

class FavouritesRepository(private val retrofitService: RetrofitService) {

    fun getFavourites(userId: String) = retrofitService.getFavourites(userId)
    fun updateFavourites(
        userId: String,
        newFavouritesList: NewFavouritesRequest
    ) = retrofitService.updateFavourites(userId, newFavouritesList)

}