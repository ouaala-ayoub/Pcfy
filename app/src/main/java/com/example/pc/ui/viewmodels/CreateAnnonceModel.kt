package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.pc.data.models.network.NewAnnonceRequest
import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.models.network.User
import com.example.pc.data.repositories.CreateAnnonceRepository
import com.example.pc.utils.getError
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//temporary

private const val TAG = "CreateAnnonceModel"

class CreateAnnonceModel(private val createAnnonceRepository: CreateAnnonceRepository) :
    ViewModel() {

    private val errorMessage = MutableLiveData<String>()
    val requestSuccessful = MutableLiveData<Boolean>()
    val titleLiveData = MutableLiveData<String>()
    val priceLiveData = MutableLiveData<String>()
    val imagesLiveData = MutableLiveData<String>()
    val isTurning = MutableLiveData<Boolean>()
    val isValidInput = MediatorLiveData<Boolean>().apply {

        addSource(titleLiveData) { title ->
            val price = priceLiveData.value
            val images = imagesLiveData.value
            this.value = validateData(title, price, images)
        }
        addSource(priceLiveData) { price ->
            val title = titleLiveData.value
            val images = imagesLiveData.value
            this.value = validateData(title, price, images)
        }
        addSource(imagesLiveData) { images ->
            val title = titleLiveData.value
            val price = priceLiveData.value
            this.value = validateData(title, price, images)
        }
    }

    //get the user id ??

    private fun validateData(title: String?, price: String?, images: String?): Boolean {

        val isValidTitle = !title.isNullOrBlank()
        val isValidPrice = !price.isNullOrBlank()
        val isValidImagesInput = !images.isNullOrBlank()

        return isValidTitle && isValidPrice && isValidImagesInput
    }

    fun addAnnonce(
        annonce: RequestBody,
    ): LiveData<Boolean> {

        isTurning.postValue(true)

        createAnnonceRepository.addAnnonce(
            annonce
        )
            .enqueue(object : Callback<IdResponse> {
                override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                    if (response.isSuccessful && response.body()?.objectId != null) {

                        Log.i(TAG, "addAnnonce response body is ${response.body()}")
                        requestSuccessful.postValue(true)

                    } else {
                        val error = response.errorBody()?.let { getError(it, response.code()) }
                        Log.e(TAG, "onResponse addAnnonce : $error")
                        requestSuccessful.postValue(false)
                    }
                    isTurning.postValue(false)
                }

                override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure addAnnonce${t.message!!}")
                    errorMessage.postValue(t.message)
                    requestSuccessful.postValue(false)
                    isTurning.postValue(false)
                }
            })

        return requestSuccessful
    }

}
