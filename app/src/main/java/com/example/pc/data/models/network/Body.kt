package com.example.pc.data.models.network


import com.google.gson.annotations.SerializedName

data class AuthBody(
    @SerializedName("body")
    val body: BodyX?
)