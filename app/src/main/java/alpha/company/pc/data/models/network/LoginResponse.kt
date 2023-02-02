package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("payload")
    val idPayload: IdPayload
)
data class IdPayload(
    @SerializedName("id")
    val id: String
)
