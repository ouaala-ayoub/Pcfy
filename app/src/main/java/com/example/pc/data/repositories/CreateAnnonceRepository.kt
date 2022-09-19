package com.example.pc.data.repositories

import com.example.pc.data.models.network.NewAnnonceRequest
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart

class CreateAnnonceRepository(private val retrofitService: RetrofitService) {
    //add calls
    fun addAnnonce(
        annonceFields: HashMap<String, String>,
    ): Call<IdResponse> = retrofitService.addAnnonce(annonceFields)

    fun addAnnonceIdToUser(
        userId: String,
        annonceId: NewAnnonceRequest
    ): Call<User> = retrofitService.updateAnnonces(userId, annonceId)

    fun getUserById(userId: String) = retrofitService.getUserById(userId)
}