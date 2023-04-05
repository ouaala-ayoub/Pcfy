package alpha.company.pc.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.data.repositories.TokensRepository
import alpha.company.pc.utils.NON_AUTHENTICATED
import alpha.company.pc.utils.getError
import androidx.lifecycle.LiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "AuthModel"

class AuthModel(
    private val retrofitService: RetrofitService,
    private val loginRepository: LoginRepository? = null,
    private val tokensRepository: TokensRepository? = TokensRepository(retrofitService)
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    private val _isTurning = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()

    val user: LiveData<User?>
        get() = _user
    val isTurning: LiveData<Boolean>
        get() = _isTurning
    val errorResponse: LiveData<String>
        get() = _errorMessage

    fun getUserById(userId: String) {
        retrofitService.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    _user.postValue(response.body())
                } else {
                    _user.postValue(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
                _user.postValue(null)
            }

        })
    }


    fun auth(
        context: Context,
    ) {

        _isTurning.postValue(true)

        retrofitService.auth().enqueue(object : Callback<User?> {

            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse: ${response.raw()}")
                    Log.i(TAG, "auth ${response.body()}")
                    _user.postValue(response.body())

                } else {

                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "onResponse not successful: $error")
                    _user.postValue(null)
                    _errorMessage.postValue(NON_AUTHENTICATED)

                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                Log.e(TAG, "auth onFailure: ${t.message}")
                _isTurning.postValue(false)
                _user.postValue(null)
                _errorMessage.postValue(alpha.company.pc.utils.ERROR_MSG)
            }
        })

    }


    fun logout(userId: String, deviceToken: String) {
        loginRepository?.logout()
        tokensRepository?.deleteToken(userId, deviceToken)
            ?.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.i(TAG, "deleteToken onResponse: deleted token success")
                    } else {
                        val error = getError(response.errorBody(), response.code())
                        Log.e(TAG, "onResponse: $error")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "deleteToken onFailure: ${t.message}")
                }

            })
    }

}