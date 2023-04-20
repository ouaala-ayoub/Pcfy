package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.PasswordRequest
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.utils.getError
import androidx.lifecycle.LiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "ChangePasswordModel"
private const val ERROR_PASSWORD = "Données incorrectes"

class ChangePasswordModel(private val userInfoRepository: UserInfoRepository) : ViewModel() {

    private val _passwordModified = MutableLiveData<Boolean>()
    private val _isTurning = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()

    val passwordModified: LiveData<Boolean>
        get() = _passwordModified
    val isTurning: LiveData<Boolean>
        get() = _isTurning
    val errorMessage: LiveData<String>
        get() = _errorMessage

    val oldPassword = MutableLiveData<String>("")
    val newPassword = MutableLiveData<String>("")
    val newPasswordRetype = MutableLiveData<String>("")

    val isValidData = MediatorLiveData<Boolean>().apply {
        addSource(oldPassword) { oldPassword ->
            this.value = validateData(oldPassword, newPasswordRetype.value, newPassword.value)
        }
        addSource(newPassword) { newPassword ->
            this.value = validateData(oldPassword.value, newPassword, newPasswordRetype.value)
        }
        addSource(newPasswordRetype) { newPasswordRetype ->
            this.value = validateData(oldPassword.value, newPassword.value, newPasswordRetype)
        }
    }

    private fun validateData(
        oldPassword: String?,
        newPassword: String?,
        newPasswordRetype: String?
    ): Boolean {
        val isValidPassword = isValidPassword(oldPassword)
        val isValidNewPassword = isValidPassword(newPassword)
        val isValidRetypedPassword = newPassword == newPasswordRetype

        return isValidPassword && isValidRetypedPassword && isValidNewPassword
    }

    private fun isValidPassword(password: String?) =
        if (password.isNullOrBlank()) false else password.length >= 8


    fun changePassword(userId: String, passwords: PasswordRequest) {

        _isTurning.postValue(true)

        userInfoRepository.changePassword(userId, passwords)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _passwordModified.postValue(true)
                    } else {
                        try {

                            if (response.code() == 500) {
                                val error = getError(response.errorBody()!!, response.code())
                                if (error?.message == "incorrect Password") {
                                    _errorMessage.postValue(ERROR_PASSWORD)
                                } else {
                                    _errorMessage.postValue(alpha.company.pc.utils.ERROR_MSG)
                                }

                            }
                        } catch (e: Throwable) {
                            _errorMessage.postValue(alpha.company.pc.utils.ERROR_MSG)
                        }

                        _passwordModified.postValue(false)

                    }
                    _isTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                    _isTurning.postValue(false)
                    _passwordModified.postValue(false)
                    _errorMessage.postValue(alpha.company.pc.utils.ERROR_MSG)
                }

            })
    }
}