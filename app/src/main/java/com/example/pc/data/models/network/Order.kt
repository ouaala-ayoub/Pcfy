package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("customerId")
    val customerId: String,

    @SerializedName("announceId")
    val announceId: String,

    @SerializedName("quantity")
    val quantity: Int = 1,

    @SerializedName("status")
    val orderStatus: String = "in progress"

)
