package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Tokens(
    @SerializedName("refreshToken")
    val refreshToken: String?,

    @SerializedName("accessToken")
    val accessToken: String?,
)