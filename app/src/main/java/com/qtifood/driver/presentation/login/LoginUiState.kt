package com.qtifood.driver.presentation.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val phoneNumber: String = "",
    val countryCode: String = "+84",
    val verificationId: String? = null,
    val isVerificationCodeSent: Boolean = false,
    val isAuthenticated: Boolean = false,
    val firebaseUid: String? = null,
    val shouldNavigateToDriverInfo: Boolean = false,
    val shouldNavigateToHome: Boolean = false
)
