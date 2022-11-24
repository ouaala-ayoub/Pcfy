package com.example.pc.data.models.network


import com.google.gson.annotations.SerializedName

data class Payload(
    @SerializedName("exp")
    val exp: Int,
    @SerializedName("iat")
    val iat: Int,
    @SerializedName("id")
    val id: String,
//    @SerializedName("name")
//    val name: String
)