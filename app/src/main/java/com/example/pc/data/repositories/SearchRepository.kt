package com.example.pc.data.repositories

import com.example.pc.data.remote.RetrofitService

class SearchRepository(private val retrofitService: RetrofitService) {

    //to change
    fun getSearchResult(searchKey: String?, price: Number?, status: String?) =
        retrofitService.getAllAnnonces(null, searchKey, price, status)

}