package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class User(

    //required = true
    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phoneNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    //non required

    @SerializedName("_id")
    val userId: String? = null,

    @SerializedName("type")
    val userType: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    // ?????????
    @SerializedName("brand")
    val brand: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("picture")
    val imageUrl: String? = null,


    //required
    @SerializedName("loginAttempts")
    val loginAttempts: Int = 0,
    //required

    @SerializedName("lockUntil")
    val lockUntil: Number? = null,

    @SerializedName("annonces")
    val annonces: MutableList<String> = mutableListOf(),

    @SerializedName("favourites")
    val favourites: MutableList<String> = mutableListOf(),
    // locally keep the favourites ?

    @SerializedName("__v")
    val version: Int? = null
)