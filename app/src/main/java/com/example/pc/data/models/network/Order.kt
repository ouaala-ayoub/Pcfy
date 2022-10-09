package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("customerId")
    val customerId: String,

    @SerializedName("announceId")
    val announceId: String,

    val quantity: Number = 1,

    val orderStatus: String = "in progress"

)
