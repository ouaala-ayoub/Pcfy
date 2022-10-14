package com.example.pc.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.repositories.AnnonceModifyRepository
import com.example.pc.utils.URIPathHelper
import com.example.pc.utils.getError
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val TAG = "AnnonceModifyModel"

class AnnonceModifyModel(private val annonceModifyRepository: AnnonceModifyRepository) :
    ViewModel() {

    //    private val errorMessage = MutableLiveData<String>()
    private val oldAnnonce = MutableLiveData<Annonce>()
    private val updatedAnnonce = MutableLiveData<Boolean>()
    val deletedImage = MutableLiveData<Boolean>()
    val titleLiveData = MutableLiveData<String>()
    val priceLiveData = MutableLiveData<String>()
    val isTurning = MutableLiveData<Boolean>()
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

    fun updateAnnonceInfo(annonceId: String, newAnnonce: Annonce): MutableLiveData<Boolean> {

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
                        Log.e(TAG, "onResponse updateAnnonceInfo : ${response.code()}")
                        updatedAnnonce.postValue(false)
                    }
                    isTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "onFailure updateAnnonceInfo: ${t.message}")
                    isTurning.postValue(false)
                }

            })
        return updatedAnnonce
    }

    fun deleteImage(annonceId: String, imageIndex: RequestBody) {

        isTurning.postValue(true)

        annonceModifyRepository.deleteImage(annonceId, imageIndex)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful && response.body() != null){
                        Log.i(TAG, "deleteImage onResponse: ${response.code()}")
                        getAnnonce(annonceId)
                        deletedImage.postValue(true)
                    }
                    else {
                        val error = getError(response.errorBody()!!, response.code())
                        Log.i(TAG, "deleteImage onResponse: error $error")
                        deletedImage.postValue(false)
                    }
                    isTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    deletedImage.postValue(false)
                    Log.e(TAG, "deleteImage onFailure: ${t.message}")
                }

            })
    }

}
