package com.qtifood.driver.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qtifood.driver.data.firebase.OrderTrackingData
import com.qtifood.driver.data.firebase.OrderTrackingDataSource
import com.qtifood.driver.data.firebase.OrderTrackingEvent
import com.qtifood.driver.domain.model.DriverLocation
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository
import com.qtifood.driver.domain.usecase.GetDriverProfileUseCase
import com.qtifood.driver.domain.usecase.UpdateDriverLocationUseCase
import com.qtifood.driver.domain.usecase.UpdateDriverStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DriverHomeViewModel(
    private val repository: DriverRepository,
    private val getDriverProfileUseCase: GetDriverProfileUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val orderTrackingDataSource: OrderTrackingDataSource,
    private val updateDriverLocationUseCase: UpdateDriverLocationUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState.asStateFlow()
    
    init {
        loadDriverProfile()
    }
    
    /**
     * Toggle online/offline status
     */
    fun toggleOnlineStatus() {
        val currentDriver = _uiState.value.driver ?: return
        val newStatus = !_uiState.value.isOnline
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val statusString = if (newStatus) "ONLINE" else "OFFLINE"
            
            when (val result = updateDriverStatusUseCase(currentDriver.id, statusString)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isOnline = newStatus,
                        isLoading = false,
                        driver = result.data,
                        isToggleEnabled = result.data.status != "BUSY"
                    )
                    
                    // Start listening to orders when online
                    if (newStatus) {
                        startOrderTracking()
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    /**
     * Update driver location
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        val driverId = _uiState.value.driver?.id ?: return
        
        // Update UI state with presentation layer DriverLocation
        _uiState.value = _uiState.value.copy(
            currentLocation = DriverLocation(latitude, longitude)
        )
        
        // Update location to Firebase with domain model DriverLocation
        if (_uiState.value.isOnline) {
            viewModelScope.launch {
                val domainLocation = com.qtifood.driver.domain.model.DriverLocation(
                    driverId = driverId,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis(),
                    isOnline = _uiState.value.isOnline
                )
                updateDriverLocationUseCase(domainLocation)
            }
        }
    }
    
    /**
     * Load driver profile
     */
    private fun loadDriverProfile() {
        viewModelScope.launch {
            val firebaseUid = repository.getCurrentFirebaseUid() ?: return@launch
            
            when (val result = getDriverProfileUseCase(firebaseUid)) {
                is Result.Success -> {
                    val driver = result.data
                    _uiState.value = _uiState.value.copy(
                        driver = driver,
                        isOnline = driver.status == "ONLINE" || driver.status == "BUSY",
                        isToggleEnabled = driver.status != "BUSY"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is Result.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }
    
    /**
     * Refresh driver data - called when returning to home screen
     */
    fun refreshDriverData() {
        loadDriverProfile()
        if (_uiState.value.isOnline) {
            //loadAvailableOrders()
        }
    }
    
    /**
     * Start listening to order tracking
     */
    fun startOrderTracking() {
        val driverId = _uiState.value.driver?.id ?: return
        
        viewModelScope.launch {
            orderTrackingDataSource.listenToDriverOrders(driverId).collect { event ->
                when (event) {
                    is OrderTrackingEvent.NewOrder -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Đơn hàng mới: #${event.data.orderId}"
                        )
                    }
                    is OrderTrackingEvent.OrderUpdated -> {
                        // Handle order update
                    }
                    is OrderTrackingEvent.OrderRemoved -> {
                        // Handle order removed
                    }
                    is OrderTrackingEvent.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = event.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Update driver location to Firebase
     */
    fun updateLocationToFirebase(orderId: Long, latitude: Double, longitude: Double) {
        orderTrackingDataSource.updateDriverLocation(orderId, latitude, longitude)
    }

    suspend fun getCurrentOrderForDriver(): OrderTrackingData? {
        val driverId = _uiState.value.driver?.id ?: return null
        return orderTrackingDataSource.getCurrentOrderForDriver(driverId)
    }

    suspend fun forceSetOffline() {
        val driver = _uiState.value.driver ?: return
        if (_uiState.value.isOnline) {
            when (val result = updateDriverStatusUseCase(driver.id, "OFFLINE")) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isOnline = false,
                        isToggleEnabled = true,
                        driver = result.data
                    )
                }
                else -> {
                    // ignore failures on shutdown
                }
            }
        }
    }
    
    /**
     * Load available orders when online
     */
    // TODO: Implement with OrderRepository when needed
    /*
    fun loadAvailableOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getCurrentOrderUseCase()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        availableOrders = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }
    */
    
    /**
     * Sign out
     */
    fun signOut() {
        repository.signOut()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
