package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Error(
    @SerializedName("message")
    val message: String
)