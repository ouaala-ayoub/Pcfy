package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.local.TokenRequest
import alpha.company.pc.data.remote.RetrofitService

class TokensRepository(private val retrofitService: RetrofitService) {

    fun getUserTokens(userId: String) = retrofitService.getUserTokens(userId)

    fun addToken(userId: String, token: String) =
        retrofitService.addFireBaseToken(userId, TokenRequest(token))

    fun deleteToken(userId: String, token: String) =
        retrofitService.deleteFireBaseToken(userId, token)
}