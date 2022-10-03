package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val clientName: String,

    @SerializedName("address")
    val clientAddress : String

)
