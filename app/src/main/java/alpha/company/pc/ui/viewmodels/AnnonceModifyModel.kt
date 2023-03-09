package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.repositories.AnnonceModifyRepository
import alpha.company.pc.utils.getError
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "AnnonceModifyModel"

class AnnonceModifyModel(private val annonceModifyRepository: AnnonceModifyRepository) :
    ViewModel() {

    val citiesList = MutableLiveData<List<String>>()
    val categoriesList = MutableLiveData<List<String>>()

    val oldAnnonce = MutableLiveData<Annonce>()
    val updatedAnnonce = MutableLiveData<Boolean>()

    val isTurning = MutableLiveData<Boolean>()

    val titleLiveData = MutableLiveData<String>()
    val priceLiveData = MutableLiveData<String>()
    val isValidInput = MediatorLiveData<Boolean>().apply {

        addSource(titleLiveData) { title ->
            val price = priceLiveData.value
            this.value = validateData(title, price)
        }
        addSource(priceLiveData) { price ->
            val title = titleLiveData.value
            this.value = validateData(title, price)
        }
    }

    private fun validateData(title: String?, price: String?): Boolean {

        val isValidTitle = !title.isNullOrBlank()
        val isValidPrice = !price.isNullOrBlank()

        return isValidTitle && isValidPrice
    }

    fun getCities() {
        annonceModifyRepository.getCities().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful && response.body() != null)
                    citiesList.postValue(response.body())
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    if (error != null)
                        Log.e(TAG, "getCategories response error $error")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e(TAG, "getCategories onFailure : ${t.message}")
            }

        })
    }

    fun getCategories() {
        annonceModifyRepository.getCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful && response.body() != null)
                    categoriesList.postValue(response.body())
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    if (error != null)
                        Log.e(TAG, "getCategories response error $error")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e(TAG, "getCategories onFailure : ${t.message}")
            }

        })
    }

    fun getAnnonce(annonceId: String): LiveData<Annonce> {

        isTurning.postValue(true)

        annonceModifyRepository.getAnnonceById(annonceId).enqueue(object : Callback<Annonce> {

            override fun onResponse(call: Call<Annonce>, response: Response<Annonce>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse: ${response.body()}")
                    oldAnnonce.postValue(response.body())
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse updateAnnonceInfo : ${error?.message}")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<Annonce>, t: Throwable) {
                Log.e(TAG, "onFailure updateAnnonceInfo: ${t.message}")
                isTurning.postValue(false)
            }

        })
        return oldAnnonce
    }

    fun triggerLoading() {
        isTurning.postValue(true)
    }

    fun updateAnnonceInfo(annonceId: String, newAnnonce: RequestBody): MutableLiveData<Boolean> {

        isTurning.postValue(true)

        annonceModifyRepository.updateAnnonce(annonceId, newAnnonce)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.i(TAG, "onResponse: ${response.body()}")
                        updatedAnnonce.postValue(true)
                    } else {
                        val error = getError(response.errorBody(), response.code())
                        Log.e(TAG, "onResponse updateAnnonceInfo : ${error}")
                        updatedAnnonce.postValue(false)
                    }
                    isTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "onFailure updateAnnonceInfo: ${t.message}")
                    updatedAnnonce.postValue(false)
                    isTurning.postValue(false)
                }

            })
        return updatedAnnonce
    }

//    fun deleteImage(annonceId: String, imageIndex: RequestBody) {
//
//        isTurning.postValue(true)
//
//        annonceModifyRepository.deleteImage(annonceId, imageIndex)
//            .enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//                    if (response.isSuccessful && response.body() != null) {
//                        Log.i(TAG, "deleteImage onResponse: ${response.code()}")
//                        getAnnonce(annonceId)
//                        deletedImage.postValue(true)
//                    } else {
//                        val error = getError(response.errorBody()!!, response.code())
//                        Log.i(TAG, "deleteImage onResponse: error $error")
//                        deletedImage.postValue(false)
//                    }
//                    isTurning.postValue(false)
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    deletedImage.postValue(false)
//                    Log.e(TAG, "deleteImage onFailure: ${t.message}")
//                }
//
//            })
//    }

//    fun changePicture(annonceId: String, imagesToPut: RequestBody) {
//        isTurning.postValue(true)
//        annonceModifyRepository.changePicture(annonceId, imagesToPut)
//            .enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//
//                    if (response.isSuccessful && response.body() != null) {
//                        Log.i(TAG, "putPictures onResponse: ${response.body()}")
//                        updatedImage.postValue(true)
//                    } else {
//                        val error = getError(response.errorBody()!!, response.code())
//                        Log.i(TAG, "putPictures onResponse error : $error")
//                        updatedImage.postValue(false)
//                    }
//
//                    isTurning.postValue(false)
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Log.e(TAG, "putPictures onFailure: ${t.message}")
//                    updatedImage.postValue(false)
//                    isTurning.postValue(false)
//                }
//
//            })
//    }
//
//    fun updatePictures(annonceId: String, imagesToPut: RequestBody) {
//
//        isTurning.postValue(true)
//
//        annonceModifyRepository.updatePictures(annonceId, imagesToPut)
//            .enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//                    if (response.isSuccessful && response.body() != null) {
//                        addedImages.postValue(true)
//                    } else {
//                        val error = getError(response.errorBody()!!, response.code())
//                        Log.i(TAG, "addImages onResponse error: $error")
//                        addedImages.postValue(false)
//                    }
//                    isTurning.postValue(false)
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Log.e(TAG, "onFailure: ${t.message}")
//                    addedImages.postValue(false)
//                    isTurning.postValue(false)
//                }
//
//            })
//    }

}
