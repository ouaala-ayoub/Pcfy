package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class NewFavouritesRequest(
    @SerializedName("favouriteId")
    val favouriteId: String
)