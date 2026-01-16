package com.qtifood.driver.domain.model

data class Order(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val status: OrderStatus,
    val createdAt: Long
)

enum class OrderStatus {
    PENDING,
    ACCEPTED,
    PICKED_UP,
    DELIVERING,
    DELIVERED,
    CANCELLED
}
