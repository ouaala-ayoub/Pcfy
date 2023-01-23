package alpha.company.pc.data.models.network


import com.google.gson.annotations.SerializedName

data class BodyX(

    @SerializedName("accessToken")
    val accessToken: String? = null,

    @SerializedName("id")
    val id: String,

//    @SerializedName("payload")
//    val payload: Payload
)