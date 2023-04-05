package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.remote.CustomMessageResponse
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.utils.getError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val PASS_MIN_LENGTH = 8
private const val TAG = "LoginModel"

class LoginModel(private val repository: LoginRepository) : ViewModel() {

    private val _retrievedTokens = MutableLiveData<Boolean>()
    private val _isTurning = MutableLiveData<Boolean>()
    private val _tokenAdded = MutableLiveData<Boolean>()

    val retrievedTokens: LiveData<Boolean>
        get() = _retrievedTokens
    val isTurning: LiveData<Boolean>
        get() = _isTurning
    val tokenAdded: LiveData<Boolean>
        get() = _tokenAdded

    val userNameLiveData = MutableLiveData<String>()
    val passwordLiveData = MutableLiveData<String>()
    private val errorMessage = MutableLiveData("")
    val isValidLiveData = MediatorLiveData<Boolean>().apply {
        addSource(userNameLiveData) { username ->
            val password = passwordLiveData.value
            this.value = validateData(username, password)
        }
        addSource(passwordLiveData) { password ->
            val email = userNameLiveData.value
            this.value = validateData(email, password)
        }
    }

    private fun validateData(email: String?, password: String?): Boolean {
        val isValidEmail = !email.isNullOrBlank() && email.contains("@")
        val isValidPassword = !password.isNullOrBlank() && password.length >= PASS_MIN_LENGTH
        return isValidEmail && isValidPassword
    }

    fun registerToken(userId: String, token: String) {
        Log.i(TAG, "registerToken: $token")
        repository.registerToken(userId, token).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    _tokenAdded.postValue(true)
                    Log.i(TAG, "putFireBaseToken isSuccessful")
                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "registerToken: $error")
                    _tokenAdded.postValue(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "putFireBaseToken : ${t.message}")
                _tokenAdded.postValue(false)
            }
        })
    }

    fun login(userName: String, password: String) {

        _isTurning.postValue(true)

        repository.login(userName, password).enqueue(object : Callback<CustomMessageResponse> {
            override fun onResponse(
                call: Call<CustomMessageResponse>,
                response: Response<CustomMessageResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse login : ${response.body()?.message}")
                    _retrievedTokens.postValue(true)
                    Log.i(TAG, "current tokens: ${repository.getCurrentTokens()}")
                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "onResponse test get error message $error")
                    errorMessage.postValue(error?.message)
                    _retrievedTokens.postValue(false)

                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<CustomMessageResponse>, t: Throwable) {
                Log.e(TAG, "onFailure login : ${t.message}")
                errorMessage.postValue(alpha.company.pc.utils.ERROR_MSG)
                _retrievedTokens.postValue(false)
                _isTurning.postValue(false)
            }
        })

    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

}