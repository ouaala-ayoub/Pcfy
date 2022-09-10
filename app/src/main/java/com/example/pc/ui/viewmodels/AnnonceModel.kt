package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pc.data.models.network.NewFavouritesRequest
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.User
import com.example.pc.data.repositories.AnnonceRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "AnnonceModel"

class AnnonceModel(private val annonceRepository: AnnonceRepository): ViewModel() {

    val annonceToShow = MutableLiveData<Annonce>()
    val seller = MutableLiveData<User>()
    val addedFavouriteToUser = MutableLiveData<Boolean>()
    val isProgressBarTurning = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()

    //add business logic
    fun getAnnonceById(annonceId: String){

        isProgressBarTurning.postValue(true)

        annonceRepository.getAnnonceById(annonceId).enqueue(
            object : Callback<Annonce>{

                override fun onResponse(call: Call<Annonce>, response: Response<Annonce>) {
                    if (response.isSuccessful && response.body() != null){
                        Log.i(TAG, "response body: ${response.body()}")
//                        annonceToReturn = response.body()
                        annonceToShow.postValue(response.body())
                        isProgressBarTurning.postValue(false)
                    }
                    else {
                        Log.i(TAG, "response error body: ${response.errorBody()}")
                        Log.i(TAG, "response raw ${response.raw()}")
                        isProgressBarTurning.postValue(false)
                    }
                }

                override fun onFailure(call: Call<Annonce>, t: Throwable) {
                    errorMessage.postValue(t.message)
                    Log.e(TAG, "onFailure: ${t.message}", )
                    isProgressBarTurning.postValue(false)
                }
            }
        )
    }

    fun getSellerById(userId: String){

        isProgressBarTurning.postValue(true)

        annonceRepository.getUserById(userId).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful && response.body() != null){
                    Log.i(TAG, "response body: ${response.body()}")
                    isProgressBarTurning.postValue(false)
                    seller.postValue(response.body())
                }
                else{
                    isProgressBarTurning.postValue(false)
                    Log.i(TAG, "response error body: ${response.errorBody()}")
                    Log.i(TAG, "response raw ${response.raw()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                isProgressBarTurning.postValue(false)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun addToFavourites(userId: String, annonceToAdd: Annonce){

        isProgressBarTurning.postValue(true)

        annonceRepository.getUserById(userId).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null){

                    //add the favourite to the favourites list
                    val favouriteList = response.body()!!.favourites
                    favouriteList.add(annonceToAdd.id!!)
                    val requestBody = NewFavouritesRequest(
                        favouriteList
                    )

                    annonceRepository.addToFavourites(userId, requestBody).enqueue(object : Callback<User>{
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful && response.body() != null){
                                addedFavouriteToUser.postValue(true)
                                isProgressBarTurning.postValue(false)
                            }
                            else{
                                Log.i(TAG, "response error body: ${response.errorBody()}")
                                Log.i(TAG, "response raw ${response.raw()}")
                                addedFavouriteToUser.postValue(false)
                                isProgressBarTurning.postValue(false)
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            errorMessage.postValue(t.message)
                            Log.e(TAG, "onFailure: ${t.message}", )
                            addedFavouriteToUser.postValue(false)
                            isProgressBarTurning.postValue(false)
                        }
                    })
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}", )
                errorMessage.postValue(t.message)
                addedFavouriteToUser.postValue(false)
                isProgressBarTurning.postValue(false)
            }
        })
    }
}