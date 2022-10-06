package com.example.pc.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.IdResponse
import com.example.pc.data.repositories.UserRepository
import com.example.pc.utils.getError
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserModel"

class UserModel(private val repository: UserRepository) : ViewModel() {

    private val userAdded = MutableLiveData<String?>()
    val isTurning = MutableLiveData<Boolean>()

    fun signUp(userToAdd: RequestBody): MutableLiveData<String?> {

        isTurning.postValue(true)

        repository.addUser(userToAdd).enqueue(object : Callback<IdResponse> {

            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {

                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse: response body = ${response.body()}")
                    userAdded.postValue(response.body()!!.objectId)
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.i(TAG, "onResponse: $error")
                    userAdded.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                userAdded.postValue(null)
                isTurning.postValue(false)
                Log.e(TAG, "error message = ${t.message}")
            }
        })
        return userAdded
    }


}
