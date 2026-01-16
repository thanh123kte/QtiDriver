package com.qtifood.driver.presentation.order

import com.qtifood.driver.domain.model.Order

data class DriverOrderUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentOrders: List<Order> = emptyList(),
    val selectedOrder: Order? = null
)
