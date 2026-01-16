package com.qtifood.driver.presentation.driverinfo

data class DriverInfoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fullName: String = "",
    val phoneNumber: String = "",
    val cccdNumber: String = "",
    val licenseNumber: String = "",
    val isFormValid: Boolean = false,
    val isSubmitSuccess: Boolean = false
)
