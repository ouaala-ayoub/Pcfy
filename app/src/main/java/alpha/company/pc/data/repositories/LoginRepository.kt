package alpha.company.pc.data.repositories

import android.content.Context
import alpha.company.pc.data.models.network.Tokens
import alpha.company.pc.data.models.network.UserCredentials
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.utils.LocalStorage
import retrofit2.Call

private const val TAG = "LoginRepository"

class LoginRepository(private val retrofitService: RetrofitService, private val activity: Context) {

    fun logout() {
        LocalStorage.deleteTokens(activity)
    }

    fun setCurrentTokens(token: Tokens) {
        LocalStorage.storeTokens(activity, token)
    }

    fun login(userName: String, password: String): Call<Tokens> {
        return retrofitService.login(UserCredentials(userName, password))
    }
}