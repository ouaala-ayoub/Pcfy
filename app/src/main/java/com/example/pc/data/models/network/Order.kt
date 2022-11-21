package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("_id")
    val orderId: String? = null,

    @SerializedName("customer")
    val customer: Customer,

    @SerializedName("announce")
    val annonce: Product,

    @SerializedName("quantity")
    val quantity: Int = 1,

    @SerializedName("status")
    var orderStatus: String = "in progress",

    )


data class Customer(
    @SerializedName("customerId")
    val id: String,

    @SerializedName("customerName")
    val name: String,

    @SerializedName("shippingAddress")
    val address: String,

    @SerializedName("number")
    val number: String
)

data class Product(
    @SerializedName("announceId")
    val id: String,

    @SerializedName("title")
    val name: String,

    @SerializedName("picture")
    val picture: String,

    @SerializedName("price")
    val price: Number
    )
