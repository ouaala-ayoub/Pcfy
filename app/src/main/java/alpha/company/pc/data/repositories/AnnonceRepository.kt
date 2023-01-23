package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.network.*
import alpha.company.pc.data.remote.RetrofitService
import okhttp3.ResponseBody
import retrofit2.Call

class AnnonceRepository(private val retrofitService: RetrofitService) {
    fun getAnnonceById(annonceId: String) = retrofitService.getAnnonceById(annonceId)

    fun getUserById(userId: String) = retrofitService.getUserById(userId)

    fun addToFavourites(userId: String, favouriteId: String): Call<ResponseBody> =
        retrofitService.addFavourite(userId, NewFavouritesRequest(favouriteId))

    fun deleteFavourite(userId: String, favouriteId: String): Call<ResponseBody> =
        retrofitService.deleteFavourite(userId, NewFavouritesRequest(favouriteId))

    fun getUserAnnonces(userId: String) = retrofitService.getAnnounces(userId)

    fun addOrder(orderToAdd: Order) = retrofitService.addOrder(orderToAdd)

    fun changeUserInfos(userId: String, userShippingInfos: UserShippingInfos) =
        retrofitService.changeUserShippingInfos(userId, userShippingInfos)
}