package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.Error
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val TAG = "UserAnnoncesModel"

class UserAnnoncesModel(
    private val userInfoRepository: UserInfoRepository,
): ViewModel() {

    private val annoncesList = MutableLiveData<List<Annonce>>()
    private val isEmpty = MutableLiveData<Boolean>()
    val isTurning = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<Error?>()
    val deletedAnnonce = MutableLiveData<Boolean>()

    fun getAnnoncesById(userId: String): MutableLiveData<List<Annonce>> {

        isTurning.postValue(true)

        userInfoRepository.getAnnonces(userId).enqueue(object: Callback<List<Annonce>> {

            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null){
                    Log.i(TAG, "onResponse body ${response.body()}")
                    annoncesList.postValue(response.body())
                    isTurning.postValue(false)
                }
                else{
                    val error = getError(response.errorBody()!!, response.code())
                    errorMessage.postValue(error)
                    Log.e(TAG, "onResponse error $error")
                    isTurning.postValue(false)
                }
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "getAnnoncesById onFailure: ${t.message}")
                isTurning.postValue(false)
            }

        })

        return annoncesList
    }

    fun deleteAnnonce(userId: String, annonceId: String): LiveData<Boolean>{

        //to add : delete the annonce id from the user object
        isTurning.postValue(true)

        userInfoRepository.deleteAnnonce(annonceId).enqueue(object: Callback<Annonce>{

            override fun onResponse(call: Call<Annonce>, response: Response<Annonce>) {
                if(response.isSuccessful && response.body() != null){
                    deletedAnnonce.postValue(true)
                    isTurning.postValue(false)
                }
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse delete annonce: ${error?.message}")
                    deletedAnnonce.postValue(false)
                    isTurning.postValue(false)
                }
            }

            override fun onFailure(call: Call<Annonce>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                deletedAnnonce.postValue(false)
                isTurning.postValue(false)
            }

        })
        return deletedAnnonce
    }

    fun updateIsEmpty(): MutableLiveData<Boolean> {
        if(annoncesList.value.isNullOrEmpty()){
            isEmpty.postValue(true)
        }
        else isEmpty.postValue(false)
        return isEmpty
    }

}