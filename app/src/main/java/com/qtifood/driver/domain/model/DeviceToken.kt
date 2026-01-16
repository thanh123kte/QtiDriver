package com.qtifood.driver.domain.model

data class DeviceToken(
    val id: Long,
    val userId: String,
    val role: String,
    val token: String,
    val platform: String,
    val createdAt: String,
    val updatedAt: String
)
