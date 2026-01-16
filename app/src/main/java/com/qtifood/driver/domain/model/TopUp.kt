package com.qtifood.driver.domain.model

data class TopUp(
    val id: Long,
    val amount: Double,
    val providerTransactionId: String,
    val status: String,
    val createdAt: String
)
