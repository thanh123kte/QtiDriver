package com.qtifood.driver.presentation.documents

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qtifood.driver.domain.repository.DriverRepository
import com.qtifood.driver.domain.model.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriverDocumentsViewModel(
    private val driverRepository: DriverRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "DriverDocumentsVM"
    }
    
    private val _uiState = MutableStateFlow(DriverDocumentsUiState())
    val uiState: StateFlow<DriverDocumentsUiState> = _uiState.asStateFlow()
    
    fun loadDocuments(driverId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = driverRepository.getDriverProfile(driverId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            driver = result.data
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
    
    fun updateDocuments(
        cccdFrontImageUrl: String?,
        cccdBackImageUrl: String?,
        licenseImageUrl: String?,
        vehicleRegistrationImageUrl: String?,
        vehiclePlateImageUrl: String?
    ) {
        val currentDriver = _uiState.value.driver ?: return
        
        Log.d(TAG, "=== Start uploading documents ===")
        Log.d(TAG, "Driver ID: ${currentDriver.id}")
        Log.d(TAG, "License URI: $licenseImageUrl")
        Log.d(TAG, "CCCD Front URI: $cccdFrontImageUrl")
        Log.d(TAG, "CCCD Back URI: $cccdBackImageUrl")
        Log.d(TAG, "Vehicle Registration URI: $vehicleRegistrationImageUrl")
        Log.d(TAG, "Vehicle Plate URI: $vehiclePlateImageUrl")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isUpdateSuccess = false) }
            
            var updatedDriver = currentDriver
            var uploadCount = 0
            
            // Upload license if provided (Upload 1/5)
            if (!licenseImageUrl.isNullOrEmpty()) {
                Log.d(TAG, "[1/5] Uploading license...")
                when (val uploadResult = driverRepository.uploadLicense(updatedDriver.id, licenseImageUrl)) {
                    is Result.Success -> {
                        updatedDriver = uploadResult.data
                        uploadCount++
                        Log.d(TAG, "[1/5] ✓ License uploaded successfully. License URL: ${updatedDriver.licenseImageUrl}")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[1/5] ✗ License upload failed: ${uploadResult.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Upload license failed: ${uploadResult.message}"
                            ) 
                        }
                        return@launch
                    }
                    is Result.Loading -> {}
                }
            } else {
                Log.d(TAG, "[1/5] Skipped - No license image selected")
            }
            
            // Upload CCCD front if provided (Upload 2/5)
            if (!cccdFrontImageUrl.isNullOrEmpty()) {
                Log.d(TAG, "[2/5] Uploading CCCD front...")
                when (val uploadResult = driverRepository.uploadCccdFront(updatedDriver.id, cccdFrontImageUrl)) {
                    is Result.Success -> {
                        updatedDriver = uploadResult.data
                        uploadCount++
                        Log.d(TAG, "[2/5] ✓ CCCD front uploaded successfully. CCCD Front URL: ${updatedDriver.cccdFrontImageUrl}")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[2/5] ✗ CCCD front upload failed: ${uploadResult.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Upload CCCD front failed: ${uploadResult.message}"
                            ) 
                        }
                        return@launch
                    }
                    is Result.Loading -> {}
                }
            } else {
                Log.d(TAG, "[2/5] Skipped - No CCCD front image selected")
            }
            
            // Upload CCCD back if provided (Upload 3/5)
            if (!cccdBackImageUrl.isNullOrEmpty()) {
                Log.d(TAG, "[3/5] Uploading CCCD back...")
                when (val uploadResult = driverRepository.uploadCccdBack(updatedDriver.id, cccdBackImageUrl)) {
                    is Result.Success -> {
                        updatedDriver = uploadResult.data
                        uploadCount++
                        Log.d(TAG, "[3/5] ✓ CCCD back uploaded successfully. CCCD Back URL: ${updatedDriver.cccdBackImageUrl}")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[3/5] ✗ CCCD back upload failed: ${uploadResult.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Upload CCCD back failed: ${uploadResult.message}"
                            ) 
                        }
                        return@launch
                    }
                    is Result.Loading -> {}
                }
            } else {
                Log.d(TAG, "[3/5] Skipped - No CCCD back image selected")
            }
            
            // Upload vehicle registration if provided (Upload 4/5)
            if (!vehicleRegistrationImageUrl.isNullOrEmpty()) {
                Log.d(TAG, "[4/5] Uploading vehicle registration...")
                when (val uploadResult = driverRepository.uploadVehicleRegistration(updatedDriver.id, vehicleRegistrationImageUrl)) {
                    is Result.Success -> {
                        updatedDriver = uploadResult.data
                        uploadCount++
                        Log.d(TAG, "[4/5] ✓ Vehicle registration uploaded successfully. Vehicle Reg URL: ${updatedDriver.vehicleRegistrationImageUrl}")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[4/5] ✗ Vehicle registration upload failed: ${uploadResult.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Upload vehicle registration failed: ${uploadResult.message}"
                            ) 
                        }
                        return@launch
                    }
                    is Result.Loading -> {}
                }
            } else {
                Log.d(TAG, "[4/5] Skipped - No vehicle registration image selected")
            }
            
            // Upload vehicle plate if provided (Upload 5/5)
            if (!vehiclePlateImageUrl.isNullOrEmpty()) {
                Log.d(TAG, "[5/5] Uploading vehicle plate...")
                when (val uploadResult = driverRepository.uploadVehiclePlate(updatedDriver.id, vehiclePlateImageUrl)) {
                    is Result.Success -> {
                        updatedDriver = uploadResult.data
                        uploadCount++
                        Log.d(TAG, "[5/5] ✓ Vehicle plate uploaded successfully. Vehicle Plate URL: ${updatedDriver.vehiclePlateImageUrl}")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[5/5] ✗ Vehicle plate upload failed: ${uploadResult.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Upload vehicle plate failed: ${uploadResult.message}"
                            ) 
                        }
                        return@launch
                    }
                    is Result.Loading -> {}
                }
            } else {
                Log.d(TAG, "[5/5] Skipped - No vehicle plate image selected")
            }
            
            Log.d(TAG, "=== Upload completed: $uploadCount images uploaded successfully ===")
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    driver = updatedDriver,
                    isUpdateSuccess = true
                ) 
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(isUpdateSuccess = false) }
    }
}
