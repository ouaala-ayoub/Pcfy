package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class NewAnnonceRequest(
    @SerializedName("announces")
    val annonces: List<String>
)
