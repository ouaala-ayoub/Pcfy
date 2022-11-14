package com.example.pc.data.models.network


import com.google.gson.annotations.SerializedName

data class MessageX(
    @SerializedName("notification")
    val notification: Notification,
    @SerializedName("token")
    val token: String
)