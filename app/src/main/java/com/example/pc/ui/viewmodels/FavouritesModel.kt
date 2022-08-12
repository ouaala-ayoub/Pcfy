package com.example.pc.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.NewFavouritesRequest
import com.example.pc.data.models.network.User
import com.example.pc.data.repositories.FavouritesRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "FavouritesModel"

class FavouritesModel(private val favouritesRepository: FavouritesRepository): ViewModel() {

    private var favouritesListLiveData = MutableLiveData<MutableList<Annonce>?>()
    private var deletedWithSuccess = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    val seller = MutableLiveData<User>()
    val isProgressBarTurning = MutableLiveData<Boolean>()

    fun getFavourites(userId: String): LiveData<MutableList<Annonce>?>{

//        deletedWithSuccess.postValue(false)
        isProgressBarTurning.postValue(true)

        favouritesRepository.getUserById(userId).enqueue(object :Callback<User>{

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful && response.body() != null){
                    Log.i(TAG, "onResponse user : ${response.body()}")
                    val favouritesIdList = response.body()!!.favourites
                    val favourites = mutableListOf<Annonce>()

                    for (id in favouritesIdList){
                        favouritesRepository.getAnnonceById(id).enqueue(object : Callback<Annonce>{

                            override fun onResponse(
                                call: Call<Annonce>,
                                response: Response<Annonce>
                            ) {
                                if (response.isSuccessful && response.body() != null){
                                    Log.i(TAG, "added element ")
                                    favourites.add(response.body()!!)
                                    favouritesListLiveData.postValue(favourites)
                                }
                                else {
                                    Log.i(TAG, "response error: ${response.errorBody()}")
                                    Log.e(TAG, "error adding elements " )
                                    isProgressBarTurning.postValue(false)
                                }
                            }

                            override fun onFailure(call: Call<Annonce>, t: Throwable) {
                                Log.e(TAG, "onFailure: ${t.message}")
                                errorMessage.postValue(t.message)
                                isProgressBarTurning.postValue(false)
                            }

                        })
                        isProgressBarTurning.postValue(false)
                    }

                }
                else{
                    Log.i(TAG, "response error: ${response.errorBody()}")
                    isProgressBarTurning.postValue(false)
                    return
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                errorMessage.postValue(t.message)
                isProgressBarTurning.postValue(false)
            }
        })
        isProgressBarTurning.postValue(false)
        return favouritesListLiveData
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteFavourite(userId: String, favouriteIdToDelete: String): MutableLiveData<Boolean>{

        isProgressBarTurning.postValue(true)


        val favourites = favouritesListLiveData.value
        val removed = favourites?.removeIf { annonce -> annonce.id == favouriteIdToDelete }
        favouritesListLiveData.postValue(favourites)

        val idsList = mutableListOf<String>()
        if (!favourites.isNullOrEmpty()) {
            for (favourite in favourites){
                idsList.add(favourite.id!!)
            }
        }

        if(removed != null && removed == true){

            favouritesRepository.updateFavourites(
                userId, NewFavouritesRequest(idsList)
            ).enqueue(object : Callback<User>{
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        deletedWithSuccess.postValue(true)
                        isProgressBarTurning.postValue(false)
                    }
                    else{
                        Log.e(TAG, "error body = ${response.errorBody()}" )
                        Log.e(TAG, "error raw = ${response.raw()}" )
                        deletedWithSuccess.postValue(false)
                        isProgressBarTurning.postValue(false)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    deletedWithSuccess.postValue(false)
                    Log.e(TAG, "onFailure delete : ${t.message}")
                }

            })
        }
        else{
            isProgressBarTurning.postValue(false)
            deletedWithSuccess.postValue(false)
        }
        return deletedWithSuccess
    }

    fun getTheSellerName(userId: String): String{
        //to do
        return ""
    }

}

