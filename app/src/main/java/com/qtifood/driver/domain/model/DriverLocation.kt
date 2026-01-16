package com.qtifood.driver.domain.model

data class DriverLocation(
    val driverId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val isOnline: Boolean
)
