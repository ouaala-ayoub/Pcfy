package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.network.IdResponse
import alpha.company.pc.data.remote.RetrofitService
import okhttp3.RequestBody
import retrofit2.Call

class CreateAnnonceRepository(private val retrofitService: RetrofitService) {
    //add calls
    fun addAnnonce(
        annonce: RequestBody,
    ): Call<IdResponse> = retrofitService.addAnnonce(
        annonce,
    )

}