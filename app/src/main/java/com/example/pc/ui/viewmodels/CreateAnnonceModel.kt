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
    private val requestSuccessful = MutableLiveData<Boolean>()
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
        userId: String,
        annonce: RequestBody,
    ): LiveData<Boolean> {

        isTurning.postValue(true)

        createAnnonceRepository.addAnnonce(
            annonce
        )
            .enqueue(object : Callback<IdResponse> {
                override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                    if (response.isSuccessful && response.body()?.objectId != null) {

                        //to ask about the id response

                        val annonceId = response.body()!!.objectId
                        Log.i(TAG, "response body is ${response.body()}")

                        createAnnonceRepository.getUserById(userId)
                            .enqueue(object : Callback<User> {
                                override fun onResponse(
                                    call: Call<User>,
                                    response: Response<User>
                                ) {
                                    if (response.isSuccessful && response.body() != null) {

                                        Log.i(TAG, "user = ${response.body()} ")

                                        val annonceBody = response.body()!!.annonces

                                        annonceBody.add(annonceId!!)
                                        val annonceReqBody = NewAnnonceRequest(annonceBody)

                                        createAnnonceRepository.addAnnonceIdToUser(
                                            //to change
                                            userId,
                                            annonceReqBody
                                        ).enqueue(object : Callback<User> {
                                            override fun onResponse(
                                                call: Call<User>,
                                                response: Response<User>
                                            ) {
                                                if (response.isSuccessful && response.body() != null) {
                                                    isTurning.postValue(false)
                                                    requestSuccessful.postValue(true)
                                                } else {
                                                    Log.e(
                                                        TAG,
                                                        "response error is addAnnonceIdToUser ${response.errorBody()} "
                                                    )
                                                    Log.i(
                                                        TAG,
                                                        "response message addAnnonceIdToUser ${response.message()} "
                                                    )
                                                    Log.i(
                                                        TAG,
                                                        "response code addAnnonceIdToUser ${response.code()} "
                                                    )
                                                    isTurning.postValue(false)
                                                    requestSuccessful.postValue(false)
                                                }
                                            }

                                            override fun onFailure(call: Call<User>, t: Throwable) {
                                                errorMessage.postValue(t.message)
                                                isTurning.postValue(false)
                                                requestSuccessful.postValue(false)
                                                Log.e(TAG, t.message!!)
                                            }
                                        })
                                    } else {
                                        val error =
                                            getError(response.errorBody()!!, response.code())
                                        Log.e(TAG, "onResponse getUserById: $error")
                                        Log.e(
                                            TAG,
                                            "response error is getUserById ${response.errorBody()} "
                                        )
                                        Log.i(
                                            TAG,
                                            "response message getUserById ${response.message()} "
                                        )
                                        Log.i(TAG, "response code getUserById ${response.code()} ")
                                        Log.i(TAG, "response raw getUserById ${response.raw()} ")
                                        isTurning.postValue(false)
                                        requestSuccessful.postValue(false)
                                    }
                                }

                                override fun onFailure(call: Call<User>, t: Throwable) {
                                    Log.e(TAG, t.message!!)
                                    requestSuccessful.postValue(false)
                                    errorMessage.postValue(t.message)
                                    isTurning.postValue(false)
                                }
                            })
                    } else {
                        val error = response.errorBody()?.let { getError(it, response.code()) }
                        Log.e(TAG, "onResponse addAnnonce : $error")
                        Log.e(TAG, "response error is addAnnonce ${response.errorBody()} ")
                        Log.i(TAG, "response message addAnnonce ${response.message()} ")
                        Log.i(TAG, "response code addAnnonce ${response.code()} ")
                        Log.i(TAG, "response raw addAnnonce ${response.raw()} ")
                        isTurning.postValue(false)
                        requestSuccessful.postValue(false)
                    }
                }

                override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                    Log.e(TAG, "addAnnonce${t.message!!}")
                    errorMessage.postValue(t.message)
                    requestSuccessful.postValue(false)
                    isTurning.postValue(false)
                }
            })

        return requestSuccessful
    }

}
