package com.example.pc.data.models.local

import com.google.gson.annotations.SerializedName

data class LoggedInUser(

    @SerializedName("id")
    val userId: String,

    @SerializedName("name")
    val userName: String,

    @SerializedName("token")
    val fireBaseToken: String? = null
)
