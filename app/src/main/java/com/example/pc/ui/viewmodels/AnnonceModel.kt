package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.*
import com.example.pc.data.repositories.AnnonceRepository
import com.example.pc.utils.getError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

private const val TAG = "AnnonceModel"

class AnnonceModel(private val annonceRepository: AnnonceRepository) : ViewModel() {

    val annonceToShow = MutableLiveData<Annonce?>()
    val isAddedToFav = MutableLiveData<Boolean>()
    val seller = MutableLiveData<User>()
    val addedFavouriteToUser = MutableLiveData<Boolean>()
    var deletedWithSuccess = MutableLiveData<Boolean>()
    val userModified = MutableLiveData<Boolean>()
    val orderAdded = MutableLiveData<Boolean>()
    val isProgressBarTurning = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()

    fun updateIsAddedToFav(userId: String, favouriteToCheckId: String) {

        isProgressBarTurning.postValue(true)

        annonceRepository.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    val annonces = response.body()!!.favourites
                    val isAddedToFavRes = favouriteToCheckId in annonces
                    Log.i(
                        TAG,
                        "updateIsAddedToFav favouriteToCheckId in annonces: $isAddedToFavRes"
                    )
                    isAddedToFav.postValue(isAddedToFavRes)
                } else {
                    try {
                        val error = getError(response.errorBody()!!, response.code())
                        Log.i(TAG, "updateIsAddedToFav : $error")
                    } catch (e: Throwable) {
                        Log.i(TAG, "getError : ${e.message}")
                    }
                }
                isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "updateIsAddedToFav onFailure: ${t.message}")
                isProgressBarTurning.postValue(false)
            }
        })
    }


    //add business logic
    fun getAnnonceById(annonceId: String) {

        isProgressBarTurning.postValue(true)

        annonceRepository.getAnnonceById(annonceId).enqueue(
            object : Callback<Annonce> {

                override fun onResponse(call: Call<Annonce>, response: Response<Annonce>) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.i(TAG, "response body: ${response.body()}")
//                        annonceToReturn = response.body()
                        annonceToShow.postValue(response.body())
                        isProgressBarTurning.postValue(false)
                    } else {
                        Log.i(TAG, "response error body: ${response.errorBody()}")
                        Log.i(TAG, "response raw ${response.raw()}")
                        annonceToShow.postValue(null)
                        isProgressBarTurning.postValue(false)
                    }
                }

                override fun onFailure(call: Call<Annonce>, t: Throwable) {
                    errorMessage.postValue(t.message)
                    Log.e(TAG, "onFailure: ${t.message}")
                    annonceToShow.postValue(null)
                    isProgressBarTurning.postValue(false)
                }
            }
        )
    }

    fun getSellerById(userId: String) {

        isProgressBarTurning.postValue(true)

        annonceRepository.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "response body: ${response.body()}")
                    isProgressBarTurning.postValue(false)
                    seller.postValue(response.body())
                } else {
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

    fun addToFavourites(userId: String, favouriteId: String) {

        isProgressBarTurning.postValue(true)

        annonceRepository.addToFavourites(userId, favouriteId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        addedFavouriteToUser.postValue(true)
                    } else {
                        addedFavouriteToUser.postValue(false)
                    }
                    isProgressBarTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "addToFavourites onFailure: ${t.message}")
                    isProgressBarTurning.postValue(false)
                    addedFavouriteToUser.postValue(false)
                }

            })
    }

    fun deleteFavourite(userId: String, favouriteId: String) {
        annonceRepository.deleteFavourite(userId, favouriteId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        deletedWithSuccess.postValue(true)
                    } else {
                        deletedWithSuccess.postValue(false)
                    }
                    isProgressBarTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "deleteFavourite onFailure: ${t.message}")
                    deletedWithSuccess.postValue(false)
                    isProgressBarTurning.postValue(false)
                }
            })
    }

    fun updateUserInfo(userId: String, userInfo: UserShippingInfos) {

        isProgressBarTurning.postValue(true)

        annonceRepository.changeUserInfos(
            userId,
            userInfo
        ).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    userModified.postValue(true)
                } else {
                    userModified.postValue(false)
                }
                isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure updateUserInfo ${t.message}")
                isProgressBarTurning.postValue(false)
                userModified.postValue(false)
            }

        })
    }

    fun addOrder(orderToAdd: Order) {

        isProgressBarTurning.postValue(true)

        annonceRepository.addOrder(orderToAdd).enqueue(object : Callback<IdResponse> {
            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                val orderId = response.body()?.objectId
                if (response.isSuccessful && orderId != null) {
                    Log.i(TAG, "addOrder onResponse: $orderId")
                    orderToAdd.apply {
                        updateUserInfo(customer.id, UserShippingInfos(
                            customer.name,
                            customer.number,
                            customer.address
                        ))
                    }

                    orderAdded.postValue(true)
                } else {
                    val error = response.errorBody()?.let { getError(it, response.code()) }
                    Log.e(TAG, "addOrder onResponse: $error")
                    orderAdded.postValue(false)
                }
                isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "addOrder onFailure: ${t.message}")
                orderAdded.postValue(false)
                isProgressBarTurning.postValue(false)
            }

        })
    }
}