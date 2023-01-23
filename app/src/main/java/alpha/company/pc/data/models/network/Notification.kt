package alpha.company.pc.data.models.network


import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String
)