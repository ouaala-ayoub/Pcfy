package alpha.company.pc.data.repositories

import android.content.Context
import alpha.company.pc.data.models.network.Tokens
import alpha.company.pc.data.models.network.UserCredentials
import alpha.company.pc.data.remote.CustomMessageResponse
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.ui.activities.freeUser
import alpha.company.pc.ui.activities.globalUserObject
import alpha.company.pc.utils.LocalStorage
import retrofit2.Call

private const val TAG = "LoginRepository"

class LoginRepository(private val activity: Context) {

    fun logout() {
        LocalStorage.deleteTokens(activity)
        globalUserObject = freeUser
    }

    fun getCurrentTokens(): Tokens {
        return LocalStorage.getTokens(activity)
    }

    fun setCurrentTokens(token: Tokens) {
        LocalStorage.storeTokens(activity, token)
    }

    fun login(userName: String, password: String): Call<CustomMessageResponse> {
        return RetrofitService.getInstance(activity).login(UserCredentials(userName, password))
    }
}