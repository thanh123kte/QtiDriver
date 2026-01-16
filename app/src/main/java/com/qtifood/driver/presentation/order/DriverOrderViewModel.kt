package com.qtifood.driver.presentation.order

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DriverOrderViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(DriverOrderUiState())
    val uiState: StateFlow<DriverOrderUiState> = _uiState.asStateFlow()
    
    // TODO: Implement business logic
    // - Get current orders
    // - Accept order
    // - Update order status (picked up, delivering, delivered)
}
