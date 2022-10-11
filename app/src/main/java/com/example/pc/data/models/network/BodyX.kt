package com.example.pc.data.models.network


import com.google.gson.annotations.SerializedName

data class BodyX(

    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("payload")
    val payload: Payload
)