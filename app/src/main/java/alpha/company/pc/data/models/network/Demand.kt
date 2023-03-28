package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Demand(

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("price")
    val price: String?,

    @SerializedName("creator")
    val creator: Creator,

    @SerializedName("picture")
    val picture: String?,

    @SerializedName("description")
    val description: String?,

    )

data class Creator(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("picture")
    val picture: String? = null
)
