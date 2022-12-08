package com.example.pc.data.repositories

import com.example.pc.data.models.local.Visited
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.remote.RetrofitService
import retrofit2.Call

class HomeRepository(private val retrofitService: RetrofitService) {
    fun getAnnonces(category: String?, searchKey: String?): Call<List<Annonce>> =
        retrofitService.getAllAnnonces(category, searchKey)

    fun getAnnoncesByCategory(category: String) =
        retrofitService.getAllAnnonces(category)

    fun getPopularAnnonces() = retrofitService.getAllAnnonces(
        visited = Visited.MOST.value
    )
}