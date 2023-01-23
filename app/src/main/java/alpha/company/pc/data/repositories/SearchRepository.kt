package alpha.company.pc.data.repositories

import alpha.company.pc.data.remote.RetrofitService

class SearchRepository(private val retrofitService: RetrofitService) {

    //to change
    fun getSearchResult(searchKey: String?, price: Number?, status: String?) =
        retrofitService.getAllAnnonces(null, searchKey, price, status)

}