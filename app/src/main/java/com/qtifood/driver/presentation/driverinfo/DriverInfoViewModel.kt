package com.qtifood.driver.presentation.driverinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.model.VerificationStatus
import com.qtifood.driver.domain.usecase.CreateDriverUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DriverInfoViewModel(
    private val createDriverUseCase: CreateDriverUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverInfoUiState())
    val uiState: StateFlow<DriverInfoUiState> = _uiState.asStateFlow()

    fun updateFullName(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name)
        validateForm()
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone)
        validateForm()
    }

    fun updateCccdNumber(cccd: String) {
        _uiState.value = _uiState.value.copy(cccdNumber = cccd)
        validateForm()
    }

    fun updateLicenseNumber(license: String) {
        _uiState.value = _uiState.value.copy(licenseNumber = license)
        validateForm()
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.fullName.isNotBlank() &&
                state.phoneNumber.isNotBlank() &&
                state.cccdNumber.isNotBlank() &&
                state.licenseNumber.isNotBlank()
        _uiState.value = state.copy(isFormValid = isValid)
    }

    fun submitDriverInfo(firebaseUid: String) {
        if (!_uiState.value.isFormValid) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng điền đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Convert phone number from +84xxxxxxxxx to 0xxxxxxxxx
            val phoneNumber = convertPhoneNumber(_uiState.value.phoneNumber)

            val driver = Driver(
                id = firebaseUid,
                fullName = _uiState.value.fullName,
                phone = phoneNumber,
                cccdNumber = _uiState.value.cccdNumber,
                licenseNumber = _uiState.value.licenseNumber,
                verificationStatus = VerificationStatus.PENDING
            )

            when (val result = createDriverUseCase(driver)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSubmitSuccess = true
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Convert phone number from international format to local format
     * +84xxxxxxxxx -> 0xxxxxxxxx
     */
    private fun convertPhoneNumber(phone: String): String {
        return when {
            phone.startsWith("+84") -> "0${phone.substring(3)}"
            phone.startsWith("84") -> "0${phone.substring(2)}"
            else -> phone
        }
    }
}
