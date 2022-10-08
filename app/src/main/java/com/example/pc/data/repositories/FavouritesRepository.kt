package com.example.pc.data.repositories

import com.example.pc.data.models.network.NewFavouritesRequest
import com.example.pc.data.remote.RetrofitService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback

class FavouritesRepository(private val retrofitService: RetrofitService) {

    fun getFavourites(userId: String) = retrofitService.getFavourites(userId)

    fun deleteFavourite(userId: String, favouriteId: String): Call<ResponseBody> =
        retrofitService.deleteFavourite(userId, NewFavouritesRequest(favouriteId))

}