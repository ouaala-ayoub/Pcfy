package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class User(

    //required = true
    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phoneNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String? = null,

    //non required

    @SerializedName("_id")
    val userId: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("token")
    val token: String? = null,

    // ?????????
    @SerializedName("brand")
    val brand: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("picture")
    val imageUrl: String?,

    //required
    @SerializedName("loginAttempts")
    val loginAttempts: Int = 0,
    //required

    @SerializedName("lockUntil")
    val lockUntil: Number? = null,


    @SerializedName("favourites")
    val favourites: MutableList<String> = mutableListOf(),
    // locally keep the favourites ?

    @SerializedName("shippingAddress")
    val address: String? = null

)

data class UserShippingInfos(

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phoneNumber: String,

    @SerializedName("shippingAddress")
    val address: String? = null,
)