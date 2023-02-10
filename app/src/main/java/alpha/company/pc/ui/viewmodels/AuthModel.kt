package alpha.company.pc.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.BodyX
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.utils.LocalStorage
import alpha.company.pc.utils.NON_AUTHENTICATED
import alpha.company.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "AuthModel"

class AuthModel(
    private val retrofitService: RetrofitService,
    private val loginRepository: LoginRepository? = null
) : ViewModel() {

    val user = MutableLiveData<User?>()
    val auth = MutableLiveData<BodyX?>()
    val isTurning = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun getUserById(userId: String) {
        retrofitService.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    user.postValue(response.body())
                } else {
                    user.postValue(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
                user.postValue(null)
            }

        })
    }

    fun auth(context: Context) {

        isTurning.postValue(true)

        val tokens = LocalStorage.getTokens(context)
        val cookies = "jwt-refresh=${tokens.refreshToken}; jwt-access=${tokens.accessToken}"
        retrofitService.auth(cookies).enqueue(object : Callback<BodyX?> {

            override fun onResponse(call: Call<BodyX?>, response: Response<BodyX?>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "auth ${response.body()}")
                    auth.postValue(response.body())
                    val newAccessToken = response.body()?.accessToken
                    if (newAccessToken != null) {
                        Log.d(TAG, "storeAccessToken: $newAccessToken")
                        LocalStorage.storeAccessToken(context, newAccessToken)
                        val test = LocalStorage.getTokens(context).accessToken
                        Log.d(TAG, "new stored access token : $test")
                    }

                } else {
//                    val error = getError(response.errorBody()!!, response.code())
//                    Log.e(TAG, "onResponse auth $error" )
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
                errorMessage.postValue(alpha.company.pc.utils.ERROR_MSG)
            }
        })

    }

    fun getUserId(): String? {
        return auth.value?.id ?: return null
    }

    fun isAuth(): Boolean {
        val auth = auth.value
        return auth != null
    }

    fun logout() {
        loginRepository?.logout()
    }

}