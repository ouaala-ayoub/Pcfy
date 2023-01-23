package alpha.company.pc.data.repositories

import alpha.company.pc.data.models.local.OrderStatusRequest
import alpha.company.pc.data.remote.RetrofitService

class OrdersRepository(private val retrofitService: RetrofitService) {
    fun getOrderById(orderId: String) = retrofitService.getOrderById(orderId)

    fun getSellerOrders(sellerId: String) = retrofitService.getSellerOrders(sellerId)

    fun getUserRequests(userId: String) = retrofitService.getUserRequests(userId)

    fun changeOrderStatus(orderId: String, statusObject: OrderStatusRequest) =
        retrofitService.changeOrderStatus(orderId, statusObject)
}