package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.network.NewFavouritesRequest
import alpha.company.pc.data.remote.RetrofitService
import okhttp3.ResponseBody
import retrofit2.Call

class FavouritesRepository(private val retrofitService: RetrofitService) {

    fun getFavourites(userId: String) = retrofitService.getFavourites(userId)

    fun deleteFavourite(userId: String, favouriteId: String): Call<ResponseBody> =
        retrofitService.deleteFavourite(userId, NewFavouritesRequest(favouriteId))

}