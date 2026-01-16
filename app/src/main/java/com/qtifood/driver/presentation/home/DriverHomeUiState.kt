package com.qtifood.driver.presentation.home

import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.Order

data class DriverHomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOnline: Boolean = false,
    val isToggleEnabled: Boolean = true,
    val currentLocation: DriverLocation? = null,
    val availableOrders: List<Order> = emptyList(),
    val driver: Driver? = null
)

data class DriverLocation(
    val latitude: Double,
    val longitude: Double
)
