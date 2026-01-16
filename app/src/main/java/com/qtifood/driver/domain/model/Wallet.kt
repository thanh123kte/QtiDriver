package com.qtifood.driver.domain.model

data class Wallet(
    val id: Long,
    val userId: String,
    val balance: Double,
    val totalDeposited: Double,
    val totalWithdrawn: Double,
    val totalEarned: Double,
    val createdAt: String,
    val updatedAt: String
)
