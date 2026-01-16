package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeliveryDto(
    @SerializedName("id") val id: Long,
    @SerializedName("orderId") val orderId: Long,
    @SerializedName("driverId") val driverId: String,
    @SerializedName("driverName") val driverName: String?,
    @SerializedName("distanceKm") val distanceKm: Double,
    @SerializedName("goodsAmount") val goodsAmount: Double,
    @SerializedName("shippingFee") val shippingFee: Double,
    @SerializedName("driverIncome") val driverIncome: Double,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("storeName") val storeName: String?,
    @SerializedName("shippingAddress") val shippingAddress: String?,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("status") val status: String,
    @SerializedName("startedAt") val startedAt: String?,
    @SerializedName("completedAt") val completedAt: String?
)
