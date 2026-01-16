package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("walletId") val walletId: Long,
    @SerializedName("amount") val amount: Double,
    @SerializedName("transactionType") val type: String, // Backend trả về "transactionType"
    @SerializedName("status") val status: String = "COMPLETED", // Backend không trả về status
    @SerializedName("balanceBefore") val balanceBefore: Double = 0.0,
    @SerializedName("balanceAfter") val balanceAfter: Double = 0.0,
    @SerializedName("description") val description: String?,
    @SerializedName("referenceId") val referenceId: String?,
    @SerializedName("referenceType") val referenceType: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String? = null // Backend không luôn trả về
)
