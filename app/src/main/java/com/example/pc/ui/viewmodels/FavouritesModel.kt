package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.User
import com.example.pc.data.repositories.FavouritesRepository
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "FavouritesModel"
private const val NO_ANNONCE = "Pas de favories"
private const val ERROR_MSG = "Erreur inattendue"

class FavouritesModel(
    private val favouritesRepository: FavouritesRepository,
) : ViewModel() {

    var favouritesListLiveData = MutableLiveData<MutableList<Annonce>?>()
    var deletedWithSuccess = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    val emptyMsg = MutableLiveData<String>()
    val seller = MutableLiveData<User>()
    val isProgressBarTurning = MutableLiveData<Boolean>()

    fun getFavourites(userId: String) {

        isProgressBarTurning.postValue(true)

        favouritesRepository.getFavourites(userId).enqueue(object : Callback<List<Annonce>> {
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse getFavourites: ${response.body()}")
                    favouritesListLiveData.postValue((response.body()!!).toMutableList())
                } else {
                    Log.e(TAG, "error body = ${response.errorBody()}")
                    Log.e(TAG, "error raw = ${response.raw()}")
                    favouritesListLiveData.postValue(null)
                }
                isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure getFavourites : ${t.message}")
                errorMessage.postValue(t.message)
                favouritesListLiveData.postValue(null)
                isProgressBarTurning.postValue(false)
            }
        })
    }


    fun deleteFavourite(userId: String, favouriteIdToDelete: String): MutableLiveData<Boolean> {

        isProgressBarTurning.postValue(true)

        favouritesRepository.deleteFavourite(userId, favouriteIdToDelete)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200) {
                        deletedWithSuccess.postValue(true)
                        val favourites = favouritesListLiveData.value
                        favourites?.removeAll { annonce ->
                            annonce.id == favouriteIdToDelete
                        }
                        favouritesListLiveData.postValue(favourites)
                    } else {
//                        deletedWithSuccess.postValue(false)
                    }
                    isProgressBarTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "deleteFavourite onFailure: ${t.message}")
//                    deletedWithSuccess.postValue(false)
                    isProgressBarTurning.postValue(false)
                }

            })
        return deletedWithSuccess
    }

    fun updateIsEmpty() {
        if (favouritesListLiveData.value?.isEmpty() == true) {
            emptyMsg.postValue(NO_ANNONCE)
        } else if (favouritesListLiveData.value == null) {
            emptyMsg.postValue(ERROR_MSG)
        } else {
            emptyMsg.postValue("")
        }
    }

}

