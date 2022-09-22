package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.Error
import com.example.pc.data.models.network.NewAnnonceRequest
import com.example.pc.data.models.network.User
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
                    isTurning.postValue(false)
                } else {
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

    fun deleteAnnonce(userId: String, annonceId: String): LiveData<Boolean> {

        //to add : delete the annonce id from the user object


        isTurning.postValue(true)

        userInfoRepository.deleteAnnonce(annonceId).enqueue(object : Callback<Annonce> {

            override fun onResponse(call: Call<Annonce>, response: Response<Annonce>) {
                if (response.isSuccessful && response.body() != null) {

                    deletedAnnonce.postValue(true)

                    val annonces = annoncesList.value
                    var idsList: List<String> = listOf()
                    val removed = annonces?.removeAll { annonce ->
                        annonce.id == annonceId
                    }
                    if (!annonces.isNullOrEmpty()) {
                        idsList = annonces.map { annonce -> annonce.id!! }
                    }
                    annoncesList.postValue(annonces)

                    if (removed != null && removed == true) {

                        userInfoRepository.updateAnnonces(userId, NewAnnonceRequest(idsList))
                            .enqueue(object : Callback<User> {

                                override fun onResponse(
                                    call: Call<User>,
                                    response: Response<User>
                                ) {
                                    Log.i(
                                        TAG,
                                        "onResponse: deleted from user annonces with success"
                                    )
                                }

                                override fun onFailure(call: Call<User>, t: Throwable) {
                                    Log.e(TAG, "onFailure: ${t.message}")
                                }

                            })

                    }
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse delete annonce: ${error?.message}")
                    deletedAnnonce.postValue(false)
                }
                isTurning.postValue(false)
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
        if (annoncesList.value.isNullOrEmpty()) {
            isEmpty.postValue(true)
        } else isEmpty.postValue(false)
        return isEmpty
    }

}