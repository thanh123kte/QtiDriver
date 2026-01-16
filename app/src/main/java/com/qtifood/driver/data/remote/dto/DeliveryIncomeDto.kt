package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeliveryIncomeDto(
    @SerializedName("period") val period: String,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("totalDeliveries") val totalDeliveries: Int,
    @SerializedName("totalIncome") val totalIncome: Double,
    @SerializedName("totalShippingFee") val totalShippingFee: Double,
    @SerializedName("totalDistance") val totalDistance: Double,
    @SerializedName("averageIncomePerDelivery") val averageIncomePerDelivery: Double,
    @SerializedName("averageDistancePerDelivery") val averageDistancePerDelivery: Double
)
