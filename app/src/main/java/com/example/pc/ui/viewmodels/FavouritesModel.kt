package com.example.pc.ui.viewmodels

import android.util.Log
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

    private var favouritesList = MutableLiveData<List<Annonce>>()
    private var deletedWithSuccess = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    val seller = MutableLiveData<User>()
    val isProgressBarTurning = MutableLiveData<Boolean>()

    fun getFavourites(userId: String): LiveData<List<Annonce>>{

//        deletedWithSuccess.postValue(false)
        isProgressBarTurning.postValue(true)

        favouritesRepository.getUserById(userId).enqueue(object :Callback<User>{

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful && response.body() != null){
                    val favouritesIdList = response.body()!!.favourites
                    val favourites = mutableListOf<Annonce>()

                    for (id in favouritesIdList){
                        favouritesRepository.getAnnonceById(id).enqueue(object : Callback<Annonce>{

                            override fun onResponse(
                                call: Call<Annonce>,
                                response: Response<Annonce>
                            ) {
                                if (response.isSuccessful && response.body() != null){
                                    favourites.add(response.body()!!)
                                }
                                else {
                                    Log.i(TAG, "response error: ${response.errorBody()}")
                                    isProgressBarTurning.postValue(false)
                                    return
                                }
                            }

                            override fun onFailure(call: Call<Annonce>, t: Throwable) {
                                Log.e(TAG, "onFailure: ${t.message}")
                                errorMessage.postValue(t.message)
                                isProgressBarTurning.postValue(false)
                                return
                            }

                        })
                        isProgressBarTurning.postValue(false)
                    }
                    favouritesList.postValue(favourites)
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
        return favouritesList
    }

    fun deleteFavourite(userId: String, favouriteIdToDelete: String): MutableLiveData<Boolean>{

//        deletedWithSuccess = MutableLiveData()
        isProgressBarTurning.postValue(true)

        //get the user first
        favouritesRepository.getUserById(userId).enqueue(object :Callback<User>{

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful && response.body() != null){

                    val favouritesList = response.body()!!.favourites

                    if (favouritesList.remove(favouriteIdToDelete)){
                        favouritesRepository.updateFavourites(
                            userId,
                            NewFavouritesRequest(favouritesList)
                        ).enqueue(object :Callback<User>{

                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                if(response.isSuccessful && response.body() != null){
                                    getFavourites(userId)
                                    Log.i(TAG, "onResponse: ${response.body()}")
                                    isProgressBarTurning.postValue(false)
                                    deletedWithSuccess.postValue(true)
                                }
                                else{
                                    Log.i(TAG, "on delete response : ${response.errorBody()}")
                                    isProgressBarTurning.postValue(false)
                                    deletedWithSuccess.postValue(false)
                                }
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                Log.e(TAG, "on delete failure Failure: ${t.message}" )
                                deletedWithSuccess.postValue(false)
                            }

                        })
                    }
                    else{
                        Log.e(TAG, "something went wrong with deleting the favourite" )
                        deletedWithSuccess.postValue(false)
                        isProgressBarTurning.postValue(false)
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "on get user Failure: ${t.message}")
                deletedWithSuccess.postValue(false)
                isProgressBarTurning.postValue(false)
            }

        })
        return deletedWithSuccess
    }

    fun getTheSellerName(userId: String): String{
        //to do
        return ""
    }

}

