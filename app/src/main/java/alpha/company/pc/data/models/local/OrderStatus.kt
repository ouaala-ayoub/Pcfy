package alpha.company.pc.data.models.local

import com.google.gson.annotations.SerializedName

enum class OrderStatus(val status: String) {
    IN_PROGRESS("pending"),
    DELIVERED("delivered"),
    CANCELED("canceled")
}
data class OrderStatusRequest(
    @SerializedName("status")
    val status: String
)