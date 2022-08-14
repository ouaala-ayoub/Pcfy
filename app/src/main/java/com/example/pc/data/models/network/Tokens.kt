package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Tokens(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String
)