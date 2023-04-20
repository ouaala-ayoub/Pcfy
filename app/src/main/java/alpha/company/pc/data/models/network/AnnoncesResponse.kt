package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class AnnoncesResponse(
    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("announces")
    val annonces: List<Annonce>
)
