package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class RefreshToken(
    @SerializedName("refreshToken")
    val refreshToken: String
)
