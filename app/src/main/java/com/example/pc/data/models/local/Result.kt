package com.example.pc.data.models.local


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("message_id")
    val messageId: String
)