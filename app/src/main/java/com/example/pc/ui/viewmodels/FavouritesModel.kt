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
import com.example.pc.data.repositories.LoginRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "FavouritesModel"

class FavouritesModel(
    private val favouritesRepository: FavouritesRepository,
): ViewModel() {

    private var favouritesListLiveData = MutableLiveData<MutableList<Annonce>?>()
    private var deletedWithSuccess = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    private val isEmpty = MutableLiveData<Boolean>()
    val seller = MutableLiveData<User>()
    val isProgressBarTurning = MutableLiveData<Boolean>()

    fun getFavourites(userId: String): LiveData<MutableList<Annonce>?>{

        isProgressBarTurning.postValue(true)

        favouritesRepository.getFavourites(userId).enqueue(object : Callback<List<Annonce>>{
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if(response.isSuccessful && response.body() != null){
                    Log.i(TAG, "onResponse getFavourites: ${response.body()}")
                    favouritesListLiveData.postValue((response.body()!!).toMutableList())
                    isProgressBarTurning.postValue(false)
                }
                else{
                    Log.e(TAG, "error body = ${response.errorBody()}" )
                    Log.e(TAG, "error raw = ${response.raw()}" )
                    favouritesListLiveData.postValue(null)
                }
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure getFavourites : ${t.message}")
                errorMessage.postValue(t.message)
                favouritesListLiveData.postValue(null)
                isProgressBarTurning.postValue(false)
            }
        })
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

    fun updateIsEmpty(): MutableLiveData<Boolean> {
        if(favouritesListLiveData.value.isNullOrEmpty()){
            isEmpty.postValue(true)
        }
        else isEmpty.postValue(false)
        return isEmpty
    }

    fun getTheSellerName(userId: String): String{
        //to do
        return ""
    }

}

