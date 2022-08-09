package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class IdResponse(
    @SerializedName("_id")
    val objectId: String? = null
)