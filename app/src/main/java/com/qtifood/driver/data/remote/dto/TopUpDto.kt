package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TopUpRequest(
    @SerializedName("amount") val amount: Double
)

data class TopUpResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("amount") val amount: Double,
    @SerializedName("providerTransactionId") val providerTransactionId: String,
    @SerializedName("paymentUrl") val paymentUrl: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String
)
