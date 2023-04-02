package alpha.company.pc.data.repositories

import alpha.company.pc.data.remote.RetrofitService
import okhttp3.RequestBody

class DemandRepository(private val retrofitService: RetrofitService) {
    fun getDemandById(demandId: String) = retrofitService.getDemandById(demandId)
    fun addDemand(requestBody: RequestBody) = retrofitService.addDemand(requestBody)
    fun getDemands(searchQuery: String? = null) = retrofitService.getDemands(searchQuery)
    fun getUserDemands(userId: String) = retrofitService.getUserDemands(userId)
    fun deleteDemand(demandId: String) = retrofitService.deleteDemand(demandId)
}