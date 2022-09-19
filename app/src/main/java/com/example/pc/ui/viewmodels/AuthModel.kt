package com.example.pc.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.AuthBody
import com.example.pc.data.models.network.Payload
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "AuthModel"

class AuthModel(
    private val retrofitService: RetrofitService,
    private val loginRepository: LoginRepository?
) : ViewModel() {

    val auth = MutableLiveData<AuthBody?>()

    fun auth(context: Context) {
        val tokens = LocalStorage.getTokens(context)
        Log.i(TAG, "auth tokens: $tokens")
        retrofitService.auth(tokens).enqueue(object : Callback<AuthBody?> {

            override fun onResponse(call: Call<AuthBody?>, response: Response<AuthBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "auth ${response.body()}")
                    auth.postValue(response.body())
                    if (isAuth()) {
                        val newAccessToken = response.body()!!.body!!.accessToken
                        LocalStorage.storeAccessToken(context, newAccessToken)
                    }
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "error: $error")
                    auth.postValue(null)
                }
            }

            override fun onFailure(call: Call<AuthBody?>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                auth.postValue(null)
            }

        })

    }

    fun getPayload(): Payload? {
        return auth.value?.body?.payload ?: return null
    }

    fun isAuth(): Boolean {
        return auth.value?.body != null
    }

    fun logout() {
        loginRepository?.logout()
    }

}