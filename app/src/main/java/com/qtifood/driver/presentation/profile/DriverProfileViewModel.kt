package com.qtifood.driver.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.VerificationStatus
import com.qtifood.driver.domain.repository.DriverRepository
import com.qtifood.driver.domain.model.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriverProfileViewModel(
    private val driverRepository: DriverRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DriverProfileUiState())
    val uiState: StateFlow<DriverProfileUiState> = _uiState.asStateFlow()
    
    private var driverId: String = ""
    
    fun loadDriverProfile(id: String) {
        driverId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = driverRepository.getDriverProfile(id)) {
                is Result.Success -> {
                    val statusText = when (result.data.verificationStatus) {
                        VerificationStatus.PENDING -> "Đang chờ duyệt"
                        VerificationStatus.APPROVED -> "Đã được duyệt"
                        VerificationStatus.REJECTED -> "Bị từ chối"
                    }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            driver = result.data,
                            approvalStatusText = statusText
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Already loading
                }
            }
        }
    }
    
    fun updateDriverProfile(
        fullName: String,
        dateOfBirth: String,
        email: String,
        address: String,
        cccdNumber: String,
        licenseNumber: String,
        avatarUri: String? = null
    ) {
        val currentDriver = _uiState.value.driver ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isUpdateSuccess = false) }
            
            // Upload avatar first if provided
            val updatedDriver = if (!avatarUri.isNullOrEmpty()) {
                when (val uploadResult = driverRepository.uploadAvatar(currentDriver.id, avatarUri)) {
                    is Result.Success -> uploadResult.data
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Upload avatar failed: ${uploadResult.message}"
                            ) 
                        }
                        return@launch
                    }
                    is Result.Loading -> currentDriver
                }
            } else {
                currentDriver
            }
            
            // Update other info
            val driverToUpdate = updatedDriver.copy(
                fullName = fullName,
                dateOfBirth = dateOfBirth,
                email = email,
                address = address,
                cccdNumber = cccdNumber,
                licenseNumber = licenseNumber
            )
            
            when (val result = driverRepository.updateDriver(driverToUpdate)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            driver = result.data,
                            isUpdateSuccess = true
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Already loading
                }
            }
        }
    }
    
    fun setAvatarUri(uri: String) {
        _uiState.update { it.copy(avatarUri = uri) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(isUpdateSuccess = false) }
    }
}
