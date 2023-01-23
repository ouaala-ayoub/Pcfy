package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.local.TokenRequest
import alpha.company.pc.data.models.network.IdResponse
import alpha.company.pc.data.remote.RetrofitService
import okhttp3.RequestBody
import retrofit2.Call

class UserRepository(private val retrofitService: RetrofitService) {

    fun addUser(userToAdd: RequestBody): Call<IdResponse> = retrofitService.addUser(userToAdd)

    fun registerToken(userId: String, token: String) =
        retrofitService.putFireBaseToken(userId, TokenRequest(token))
}