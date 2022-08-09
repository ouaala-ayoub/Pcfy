package com.example.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class NewFavouritesRequest(
    @SerializedName("favourites")
    val favourites: List<String> = listOf()
)