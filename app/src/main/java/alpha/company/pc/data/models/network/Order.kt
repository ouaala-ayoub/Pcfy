package alpha.company.pc.data.models.network

import alpha.company.pc.data.models.local.OrderStatus
import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("seller")
    val seller: IdResponse,

    @SerializedName("customer")
    val customer: Customer,

    @SerializedName("announce")
    val annonce: Product,

    @SerializedName("quantity")
    val quantity: Int = 1,

    @SerializedName("status")
    var orderStatus: String = OrderStatus.IN_PROGRESS.status,

    @SerializedName("createdAt")
    var createdAt: String? = null,

    )


data class Customer(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("shippingAddress")
    val address: String,

    @SerializedName("phone")
    val number: String
)

data class Product(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val name: String,

    @SerializedName("picture")
    val picture: String,

    @SerializedName("price")
    val price: Number
)
