package alpha.company.pc.ui.viewmodels

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.IdResponse
import alpha.company.pc.data.models.network.Tokens
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.utils.USERS_AWS_S3_LINK
import alpha.company.pc.utils.getError
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserInfoModel"

class UserInfoModel(
    private val userInfoRepository: UserInfoRepository,
    private val picasso: Picasso? = null
) : ViewModel() {

    val updatedPicture = MutableLiveData<Boolean>()
    val deletedPicture = MutableLiveData<Boolean>()
    val userRetrieved = MutableLiveData<User>()
    val isTurning = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()


    fun triggerLoading() {
        isTurning.postValue(true)
    }

    fun getUserById(userId: String): LiveData<User> {

        isTurning.postValue(true)

        userInfoRepository.getUserById(userId).enqueue(object : Callback<User> {

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "getUserById onResponse: ${response.body()}")
                    userRetrieved.postValue(response.body())

                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "error body : $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                error.postValue(t.message)
                Log.e(TAG, "onFailure: ${error.value}")
                isTurning.postValue(false)
            }
        })

        return userRetrieved
    }

    fun updateImage(userId: String, image: RequestBody) {

        isTurning.postValue(true)

        userInfoRepository.updateUserImage(userId, image).enqueue(object : Callback<IdResponse> {
            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "updateImage onResponse: ${response.body()}")
                    getUserById(response.body()!!.objectId!!)
                    updatedPicture.postValue(true)
                } else {
                    Log.i(TAG, "updateImage response is no Successful: ${response.code()}")
                    val error = getError(response.errorBody()!!, response.code())
                    Log.i(TAG, "updateImage error : $error")
                    updatedPicture.postValue(false)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "updateImage onFailure: ${t.message}")
                updatedPicture.postValue(false)
                isTurning.postValue(false)
            }
        })
    }

    fun deleteProfilePicture(userId: String, tokens: RequestBody) {

        isTurning.postValue(true)

        userInfoRepository.deleteUserImage(userId, tokens).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    deletedPicture.postValue(true)
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.i(TAG, "onResponse error: $error")
                    deletedPicture.postValue(false)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "deleteProfilePicture onFailure: ${t.message}")
                deletedPicture.postValue(false)
                isTurning.postValue(false)
            }
        })
    }

    fun getRequestBody(tokens: Tokens?): RequestBody? {
        return if (tokens?.accessToken == null || tokens.refreshToken == null) {
            null
        } else {
            val builder = MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("accessToken", tokens.accessToken)
                .addFormDataPart("refreshToken", tokens.refreshToken)

            builder.build()
        }
    }

    fun loadUserImageNoCache(imageName: String, imageView: ImageView) {
        picasso
            ?.load("$USERS_AWS_S3_LINK$imageName")
            ?.fit()
            ?.networkPolicy(NetworkPolicy.NO_CACHE)
            ?.memoryPolicy(MemoryPolicy.NO_CACHE)
            ?.into(imageView)
    }

    fun loadUserImageFromCache(imageName: String, imageView: ImageView) {
        picasso
            ?.load("$USERS_AWS_S3_LINK$imageName")
            ?.fit()
            ?.into(imageView)
    }
}