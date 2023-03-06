package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Error(
    @SerializedName("message")
    var message: String?,

    val code: Int
)