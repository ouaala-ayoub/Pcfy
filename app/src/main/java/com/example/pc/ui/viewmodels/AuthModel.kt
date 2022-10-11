package com.example.pc.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.BodyX
import com.example.pc.data.models.network.Payload
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.NON_AUTHENTICATED
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "AuthModel"

class AuthModel(
    private val retrofitService: RetrofitService,
    private val loginRepository: LoginRepository?
) : ViewModel() {

    val auth = MutableLiveData<BodyX?>()
    val isTurning = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun auth(context: Context) {

        isTurning.postValue(true)

        val tokens = LocalStorage.getTokens(context)
        Log.i(TAG, "auth tokens: $tokens")
        retrofitService.auth(tokens).enqueue(object : Callback<BodyX?> {

            override fun onResponse(call: Call<BodyX?>, response: Response<BodyX?>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "auth ${response.body()}")
                    auth.postValue(response.body())
                    if (isAuth()) {
                        val newAccessToken = response.body()!!.accessToken
                        LocalStorage.storeAccessToken(context, newAccessToken)
                    }
                } else {
//                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "non authenticated")
                    auth.postValue(null)
                    errorMessage.postValue(NON_AUTHENTICATED)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<BodyX?>, t: Throwable) {
                Log.e(TAG, "auth onFailure: ${t.message}")
                isTurning.postValue(false)
                auth.postValue(null)
                errorMessage.postValue(com.example.pc.utils.ERROR_MSG)
            }
        })

    }

    fun getPayload(): Payload? {
        return auth.value?.payload ?: return null
    }

    fun isAuth(): Boolean {
        val auth = auth.value
        return auth != null
    }

    fun logout() {
        loginRepository?.logout()
    }

}