package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("data")
    val data: Data,

    @SerializedName("to")
    val token: String
)

data class Data(
    @SerializedName("annonceName")
    val annonceName: String,

    @SerializedName("sellerId")
    val sellerId: String,

    @SerializedName("orderId")
    val orderId: String
)