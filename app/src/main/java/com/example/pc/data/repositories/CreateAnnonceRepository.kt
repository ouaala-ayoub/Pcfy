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
import retrofit2.http.Part

class CreateAnnonceRepository(private val retrofitService: RetrofitService) {
    //add calls
    fun addAnnonce(
        annonce: RequestBody,
    ): Call<IdResponse> = retrofitService.addAnnonce(
        annonce,
    )

}