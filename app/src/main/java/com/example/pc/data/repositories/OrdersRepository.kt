package com.example.pc.data.repositories

import com.example.pc.data.remote.RetrofitService

class OrdersRepository(private val retrofitService: RetrofitService) {
    fun getOrderById(orderId: String) = retrofitService.getOrderById(orderId)
    fun getSellerOrders(sellerId: String) = retrofitService.getSellerOrders(sellerId)
    fun getUserRequests(userId: String) = retrofitService.getUserRequests(userId)
}