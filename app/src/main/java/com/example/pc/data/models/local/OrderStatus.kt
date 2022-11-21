package com.example.pc.data.models.local

enum class OrderStatus(val status: String) {
    IN_PROGRESS("in progress"),
    DELIVERED("delivered"),
    CANCELED("canceled")
}