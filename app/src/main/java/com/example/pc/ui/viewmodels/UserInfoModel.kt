package com.example.pc.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.User
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserInfoModel"

class UserInfoModel(
    private val userInfoRepository: UserInfoRepository,
    private val loginRepository: LoginRepository
    ) : ViewModel(){

    private val user = MutableLiveData<User>()
    val isTurning = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun getUserById(userId: String): LiveData<User> {

        isTurning.postValue(true)

        userInfoRepository.getUserById(userId).enqueue(object: Callback<User>{

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null){
                    user.postValue(response.body())
                    isTurning.postValue(false)
                }
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "error body : $error")
                    isTurning.postValue(false)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                error.postValue(t.message)
                Log.e(TAG, "onFailure: ${error.value}")
                isTurning.postValue(false)
            }
        })

        return user
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentUser(): LoggedInUser? {
        return loginRepository.user
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIsLoggedIn(): LiveData<Boolean> {
        return loginRepository.isLoggedIn
    }
}