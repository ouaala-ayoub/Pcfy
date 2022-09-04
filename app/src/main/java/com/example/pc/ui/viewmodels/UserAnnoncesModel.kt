package com.example.pc.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.Error
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val TAG = "UserAnnoncesModel"

class UserAnnoncesModel(
    private val userInfoRepository: UserInfoRepository,
    private val loginRepository: LoginRepository
): ViewModel() {

    private val annoncesList = MutableLiveData<List<Annonce>>()
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

        //to do

        return deletedAnnonce
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIsLoggedIn(): MutableLiveData<Boolean> {
        return loginRepository.isLoggedIn
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentUser(): LoggedInUser? {
        return loginRepository.user
    }

}