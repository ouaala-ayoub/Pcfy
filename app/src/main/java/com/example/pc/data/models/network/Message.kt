package com.example.pc.data.models.network


import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message")
    val message: MessageX
)