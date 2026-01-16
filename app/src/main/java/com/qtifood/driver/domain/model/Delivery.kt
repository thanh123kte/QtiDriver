package com.qtifood.driver.domain.model

data class Delivery(
    val id: Long,
    val orderId: Long,
    val driverId: String,
    val driverName: String?,
    val distanceKm: Double,
    val goodsAmount: Double,
    val shippingFee: Double,
    val driverIncome: Double,
    val paymentMethod: String,
    val storeName: String?,
    val shippingAddress: String?,
    val customerName: String?,
    val status: String,
    val startedAt: String?,
    val completedAt: String?
) : java.io.Serializable

data class DeliveryIncome(
    val period: String,
    val startDate: String?,
    val endDate: String?,
    val totalDeliveries: Int,
    val totalIncome: Double,
    val totalShippingFee: Double,
    val totalDistance: Double,
    val averageIncomePerDelivery: Double,
    val averageDistancePerDelivery: Double
)
