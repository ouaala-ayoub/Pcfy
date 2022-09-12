package com.example.pc.data.repositories

import com.example.pc.data.remote.RetrofitService

class SearchRepository (private val retrofitService: RetrofitService){

    //to change
    fun getResult() = retrofitService.search()

}