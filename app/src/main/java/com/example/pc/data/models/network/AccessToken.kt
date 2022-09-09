package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class AccessToken(
    @SerializedName("accessToken")
    val accessToken: String
)
