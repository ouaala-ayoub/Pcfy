package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.remote.RetrofitService
import okhttp3.RequestBody

class AnnonceModifyRepository(private val retrofitService: RetrofitService) {

//    fun deleteImage(annonceId: String, imageIndex: RequestBody) =
//        retrofitService.deleteAnnonceImage(annonceId, imageIndex)

//    fun changePicture(annonceId: String, imagesToPut: RequestBody) =
//        retrofitService.changePicture(annonceId, imagesToPut)

//    fun updatePictures(annonceId: String, imagesToAdd: RequestBody)=
//        retrofitService.updatePictures(annonceId, imagesToAdd)

    fun updateAnnonce(annonceId: String, newAnnonce: RequestBody) =
        retrofitService.updateAnnonceInfo(annonceId, newAnnonce)

    fun getAnnonceById(annonceId: String) = retrofitService.getAnnonceById(annonceId)

    fun getCategories() = retrofitService.getCategories()

    fun getCities() = retrofitService.getCities()
}