package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Demand(

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("price")
    val price: String,

    @SerializedName("pictures")
    val pictures: List<String>,

)
