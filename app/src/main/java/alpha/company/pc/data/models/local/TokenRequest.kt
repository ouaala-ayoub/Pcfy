package alpha.company.pc.data.models.local

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("token")
    val token: String
)