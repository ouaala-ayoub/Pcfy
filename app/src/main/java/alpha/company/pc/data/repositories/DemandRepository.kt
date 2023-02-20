package alpha.company.pc.data.repositories

import alpha.company.pc.data.remote.RetrofitService
import okhttp3.RequestBody

class DemandRepository(private val retrofitService: RetrofitService) {
    fun addDemand(requestBody: RequestBody) = retrofitService.addDemand(requestBody)
    fun getDemands() = retrofitService.getDemands()
}