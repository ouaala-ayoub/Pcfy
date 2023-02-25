package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.AccessToken
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.BodyX
import alpha.company.pc.data.models.network.RefreshToken
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.utils.LocalStorage
import alpha.company.pc.utils.NON_AUTHENTICATED
import alpha.company.pc.utils.PayloadClass
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

    private fun refresh(
        accessToken: String,
        refreshToken: String,
        onRefresh: (accessToken: String) -> Unit,
        onFailRefresh: () -> Unit
    ) {
        retrofitService.refresh(accessToken, RefreshToken(refreshToken))
            .enqueue(object : Callback<AccessToken> {
                override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                    if (response.isSuccessful && response.body() != null) {
                        val accessToken = response.body()!!.accessToken
                        Log.i(TAG, "refresh: new accessToken = $accessToken")
                        onRefresh(accessToken)
                    } else {
                        val error = getError(response.errorBody(), response.code())
                        Log.e(TAG, "refresh response is not successful $error")
                        onFailRefresh()
                    }
                }

                override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                    Log.e(TAG, "refresh onFailure: ${t.message}")
                    onFailRefresh()
                }

            })
    }

    fun auth(
        context: Context,
    ) {

        isTurning.postValue(true)

        retrofitService.auth().enqueue(object : Callback<User?> {

            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "auth ${response.body()}")
                    user.postValue(response.body())

                } else {

                    val tokens = LocalStorage.getTokens(context)
                    if (tokens.accessToken != null && tokens.refreshToken != null) {
                        val payload = PayloadClass.getInfoFromJwt(tokens.accessToken)
                        fun onRefreshFail() {
                            user.postValue(null)
                            errorMessage.postValue(NON_AUTHENTICATED)
                            Log.e(TAG, "onRefreshFail: error refreshing the token")
                        }

                        fun onRefreshSuccess(newAccessToken: String) {
                            LocalStorage.storeAccessToken(context, newAccessToken)
                            auth(context)
                        }

                        if (payload?.isExpired() == true) {
                            refresh(
                                tokens.accessToken,
                                tokens.refreshToken,
                                ::onRefreshSuccess,
                                ::onRefreshFail
                            )
                        } else {
                            onRefreshFail()
                        }


                    } else {
                        user.postValue(null)
                        errorMessage.postValue(NON_AUTHENTICATED)
                    }

                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                Log.e(TAG, "auth onFailure: ${t.message}")
                isTurning.postValue(false)
                user.postValue(null)
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