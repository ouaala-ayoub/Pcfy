package com.example.pc.data.repositories

import com.example.pc.data.models.network.Annonce
import com.example.pc.data.remote.RetrofitService

class AnnonceModifyRepository(private val retrofitService: RetrofitService) {
    fun updateAnnonce(annonceId: String, newAnnonce: Annonce) =
        retrofitService.updateAnnonceInfo(annonceId, newAnnonce)

    fun getAnnonceById(annonceId: String) = retrofitService.getAnnonceById(annonceId)
}