package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.PasswordRequest
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.utils.getError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "ChangePasswordModel"
private const val ERROR_PASSWORD = "Données incorrectes"

class ChangePasswordModel(private val userInfoRepository: UserInfoRepository) : ViewModel() {

    val passwordModified = MutableLiveData<Boolean>()
    val isTurning = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

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

        isTurning.postValue(true)

        userInfoRepository.changePassword(userId, passwords)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        passwordModified.postValue(true)
                    } else {
                        try {

                            if (response.code() == 500) {
                                val error = getError(response.errorBody()!!, response.code())
                                if (error?.message == "incorrect Password") {
                                    errorMessage.postValue(ERROR_PASSWORD)
                                } else {
                                    errorMessage.postValue(com.example.pc.utils.ERROR_MSG)
                                }

                            }
                        } catch (e: Throwable) {
                            errorMessage.postValue(com.example.pc.utils.ERROR_MSG)
                        }

                        passwordModified.postValue(false)

                    }
                    isTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "onFailure: ")
                    isTurning.postValue(false)
                    passwordModified.postValue(false)
                    errorMessage.postValue(com.example.pc.utils.ERROR_MSG)
                }

            })
    }
}