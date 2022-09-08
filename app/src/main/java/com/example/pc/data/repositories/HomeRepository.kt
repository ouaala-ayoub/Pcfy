package com.example.pc.data.repositories

import com.example.pc.data.models.network.Annonce
import com.example.pc.data.remote.RetrofitService
import retrofit2.Call

class HomeRepository (private val retrofitService: RetrofitService){
    fun getAnnonces(): Call<List<Annonce>> = retrofitService.getAllAnnonces()
}