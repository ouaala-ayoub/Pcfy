package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.local.TokenRequest
import android.content.Context
import alpha.company.pc.data.models.network.Tokens
import alpha.company.pc.data.models.network.UserCredentials
import alpha.company.pc.data.remote.CustomMessageResponse
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.utils.LocalStorage
import retrofit2.Call


class LoginRepository(private val activity: Context) {

    val retrofitService = RetrofitService.getInstance(activity)
    fun registerToken(userId: String, token: String) =
        RetrofitService.getInstance(activity).addFireBaseToken(userId, TokenRequest(token))

    fun logout() {
        LocalStorage.deleteTokens(activity)
    }

    fun getCurrentTokens(): Tokens {
        return LocalStorage.getTokens(activity)
    }

    fun setCurrentTokens(token: Tokens) {
        LocalStorage.storeTokens(activity, token)
    }

    fun login(userName: String, password: String): Call<CustomMessageResponse> {
        return retrofitService.login(UserCredentials(userName, password))
    }
}