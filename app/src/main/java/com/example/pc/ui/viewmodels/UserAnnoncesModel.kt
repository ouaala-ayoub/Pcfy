package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.*
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val TAG = "UserAnnoncesModel"

class UserAnnoncesModel(
    private val userInfoRepository: UserInfoRepository,
) : ViewModel() {

    private val annoncesList = MutableLiveData<MutableList<Annonce>?>()
    val ordersList = MutableLiveData<List<Order>?>()
    private val isEmpty = MutableLiveData<Boolean>()
    val isTurning = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<Error?>()
    val deletedAnnonce = MutableLiveData<Boolean>()

    fun getAnnoncesById(userId: String): MutableLiveData<MutableList<Annonce>?> {

        isTurning.postValue(true)

        userInfoRepository.getAnnonces(userId).enqueue(object : Callback<List<Annonce>> {

            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse body ${response.body()}")
                    annoncesList.postValue(response.body()!!.toMutableList())
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    errorMessage.postValue(error)
                    Log.e(TAG, "onResponse error $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "getAnnoncesById onFailure: ${t.message}")
                isTurning.postValue(false)
            }

        })

        return annoncesList
    }

    fun deleteAnnonce(tokens: Tokens, annonceId: String) {

        isTurning.postValue(true)

        userInfoRepository.deleteAnnonce(tokens, annonceId).enqueue(object : Callback<IdResponse> {

            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "deleteAnnonce onResponse: ${response.body()}")
                    deletedAnnonce.postValue(true)

                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse delete annonce: ${error?.message}")
                    deletedAnnonce.postValue(false)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "onFailure deleteAnnonce : ${t.message}")
                deletedAnnonce.postValue(false)
                isTurning.postValue(false)
            }

        })

    }

    fun getAnnonceOrders(annonceId: String) {

        isTurning.postValue(true)

        userInfoRepository.getAnnonceOrders(annonceId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful && response.body() != null) {
                    ordersList.postValue(response.body())
                } else {
                    ordersList.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                Log.e(TAG, "getAnnonceOrders onFailure: ${t.message}")
                ordersList.postValue(null)
                isTurning.postValue(false)
            }
        })
    }

    fun updateIsEmpty(): MutableLiveData<Boolean> {
        if (annoncesList.value.isNullOrEmpty()) {
            isEmpty.postValue(true)
        } else isEmpty.postValue(false)
        return isEmpty
    }

}