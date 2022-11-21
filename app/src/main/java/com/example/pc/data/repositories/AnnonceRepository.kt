package com.example.pc.data.repositories

import com.example.pc.data.models.network.Customer
import com.example.pc.data.models.network.NewFavouritesRequest
import com.example.pc.data.models.network.Order
import com.example.pc.data.models.network.Product
import com.example.pc.data.remote.RetrofitService
import okhttp3.ResponseBody
import retrofit2.Call

class AnnonceRepository(private val retrofitService: RetrofitService) {
    fun getAnnonceById(annonceId: String) = retrofitService.getAnnonceById(annonceId)

    fun getUserById(userId: String) = retrofitService.getUserById(userId)

    fun addToFavourites(userId: String, favouriteId: String): Call<ResponseBody> =
        retrofitService.addFavourite(userId, NewFavouritesRequest(favouriteId))

    fun deleteFavourite(userId: String, favouriteId: String): Call<ResponseBody> =
        retrofitService.deleteFavourite(userId, NewFavouritesRequest(favouriteId))

    fun getUserAnnonces(userId: String) = retrofitService.getAnnounces(userId)

//    fun addOrder(
//        userId: String,
//        userName: String,
//        shippingAddress: String,
//        phoneNumber: String,
//        annonceId: String,
//        annonceTitle: String,
//        annoncePicture: String,
//        annoncePrice: Number,
//        quantity: Int
//    ) =
//        //to ask about
//        retrofitService.addOrder(

//        )

    fun addOrder(orderToAdd: Order) = retrofitService.addOrder(orderToAdd)
}