package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.network.AnnoncesResponse
import alpha.company.pc.data.remote.RetrofitService
import retrofit2.Call

class SearchRepository(private val retrofitService: RetrofitService) {

    //to change
    fun getSearchResult(
        searchKey: String?,
        price: Number?,
        status: String?
    ): Call<AnnoncesResponse> =
        retrofitService.getAllAnnonces(null, searchKey, price, status)

}