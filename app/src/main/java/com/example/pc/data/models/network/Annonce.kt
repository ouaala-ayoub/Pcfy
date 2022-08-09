package com.example.pc.data.models.network

import com.example.pc.data.models.local.Detail
import com.google.gson.annotations.SerializedName

data class Annonce(

    //required = true

    @SerializedName("title")
    val title: String = "Rx 580",

    @SerializedName("price")
    val price: Number = 2500,

    @SerializedName("pictures")
    val pictures: MutableList<String> = mutableListOf(),

    @SerializedName("category")
    val category: String = Category.GAMER.title,

    @SerializedName("status")
    val status: String = "neuf",


    @SerializedName("sellerId")
    val sellerId: String = "",

    //non required



    @SerializedName("details")
    val details: List<Detail>? = null,

    @SerializedName("mark")
    val mark: String? = null,

    @SerializedName("description")
    val description: String? = null,


    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("__v")
    val v: Int? = null,
){


}