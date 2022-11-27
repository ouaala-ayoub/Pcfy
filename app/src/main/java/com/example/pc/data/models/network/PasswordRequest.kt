package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class PasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)
