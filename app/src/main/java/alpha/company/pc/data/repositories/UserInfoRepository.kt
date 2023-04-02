package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.network.PasswordRequest
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.remote.RetrofitService
import okhttp3.RequestBody

class UserInfoRepository(private val retrofitService: RetrofitService) {

    fun getUserById(userId: String) = retrofitService.getUserById(userId)

    fun getAnnonces(userId: String) = retrofitService.getAnnounces(userId)

    fun getAnnonceOrders(annonceId: String) = retrofitService.getAnnonceOrders(annonceId)

    fun deleteAnnonce(annonceId: String) =
        retrofitService.deleteAnnonce(
            annonceId
        )

    fun updateUserInfo(userId: String, newUser: User) =
        retrofitService.updateUserInfo(userId, newUser)

    fun updateUserImage(userId: String, image: RequestBody) =
        retrofitService.updateProfilePicture(userId, image)

    fun deleteUserImage(userId: String, tokens: RequestBody) =
        retrofitService.deleteProfilePicture(userId, tokens)

    fun changePassword(userId: String, passwords: PasswordRequest) =
        retrofitService.changePassword(userId, passwords)

}