package com.example.pc.data.repositories

import com.example.pc.data.models.network.Annonce
import com.example.pc.data.remote.RetrofitService
import okhttp3.RequestBody

class AnnonceModifyRepository(private val retrofitService: RetrofitService) {

    fun deleteImage(annonceId: String, imageIndex: RequestBody) =
        retrofitService.deleteAnnonceImage(annonceId, imageIndex)

    fun changePicture(annonceId: String, imagesToPut: RequestBody) =
        retrofitService.changePicture(annonceId, imagesToPut)

    fun addPictures(annonceId: String, imagesToAdd: RequestBody)=
        retrofitService.putPictures(annonceId, imagesToAdd)

    fun updateAnnonce(annonceId: String, newAnnonce: Annonce) =
        retrofitService.updateAnnonceInfo(annonceId, newAnnonce)

    fun getAnnonceById(annonceId: String) = retrofitService.getAnnonceById(annonceId)
}