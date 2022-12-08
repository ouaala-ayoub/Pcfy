package com.example.pc.data.models.network

import com.example.pc.data.models.local.Detail
import com.example.pc.data.models.local.LoggedInUser
import com.google.gson.annotations.SerializedName

data class Annonce(

    //required = true

    @SerializedName("title")
    val title: String,

    @SerializedName("price")
    val price: Number,

    @SerializedName("category")
    val category: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("pictures")
    val pictures: List<String>,

    //just for testing
    @SerializedName("seller")
    val seller: Seller,

    //non required
//    not needed ?
    @SerializedName("visited")
    val visited: Int? = null,

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
)

data class Seller(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("picture")
    val picture: String?
)