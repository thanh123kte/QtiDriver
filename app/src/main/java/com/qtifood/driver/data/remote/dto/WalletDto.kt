package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WalletDto(
    @SerializedName("id") val id: Long,
    @SerializedName("userId") val userId: String,
    @SerializedName("balance") val balance: Double,
    @SerializedName("totalDeposited") val totalDeposited: Double,
    @SerializedName("totalWithdrawn") val totalWithdrawn: Double,
    @SerializedName("totalEarned") val totalEarned: Double,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)
