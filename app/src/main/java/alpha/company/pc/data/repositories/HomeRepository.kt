package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.local.VisitedEnum
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.remote.RetrofitService
import retrofit2.Call

class HomeRepository(private val retrofitService: RetrofitService) {
    fun getAnnonces(category: String?, searchKey: String?): Call<List<Annonce>> =
        retrofitService.getAllAnnonces(category, searchKey)

    fun getAnnoncesByCategory(category: String) =
        retrofitService.getAllAnnonces(category)

    fun getPopularAnnonces() = retrofitService.getAllAnnonces(
        visited = VisitedEnum.MOST.value
    )

    fun getCategories() = retrofitService.getCategories()
}