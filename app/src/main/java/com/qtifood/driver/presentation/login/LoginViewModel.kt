package com.qtifood.driver.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.qtifood.driver.data.firebase.FCMTokenManager
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository
import com.qtifood.driver.domain.usecase.GetDriverByFirebaseUidUseCase
import com.qtifood.driver.domain.usecase.SignInWithPhoneUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInWithPhoneUseCase: SignInWithPhoneUseCase,
    private val getDriverByFirebaseUidUseCase: GetDriverByFirebaseUidUseCase,
    private val driverRepository: DriverRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone)
    }
    
    fun updateCountryCode(code: String) {
        _uiState.value = _uiState.value.copy(countryCode = code)
    }
    
    fun getFullPhoneNumber(): String {
        return _uiState.value.countryCode + _uiState.value.phoneNumber
    }
    
    fun setVerificationId(verificationId: String) {
        _uiState.value = _uiState.value.copy(
            verificationId = verificationId,
            isVerificationCodeSent = true
        )
    }
    
    fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = signInWithPhoneUseCase(credential)) {
                is Result.Success -> {
                    val firebaseUid = result.data
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        firebaseUid = firebaseUid
                    )
                    checkDriverExists(firebaseUid)
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
    
    private fun checkDriverExists(firebaseUid: String) {
        viewModelScope.launch {
            when (val result = getDriverByFirebaseUidUseCase(firebaseUid)) {
                is Result.Success -> {
                    val driver = result.data
                    if (driver == null) {
                        // Driver doesn't exist, navigate to registration
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            shouldNavigateToDriverInfo = true
                        )
                    } else {
                        // Driver exists, navigate to home
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            shouldNavigateToHome = true
                        )
                        // Register device token after successful login
                        registerDeviceToken(driver.id)
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
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(
            shouldNavigateToDriverInfo = false,
            shouldNavigateToHome = false
        )
    }
    
    fun isPhoneNumberValid(): Boolean {
        val phone = _uiState.value.phoneNumber
        return phone.length >= 9 && phone.all { it.isDigit() }
    }
    
    private fun registerDeviceToken(userId: String) {
        viewModelScope.launch {
            try {
                val fcmToken = FCMTokenManager.getToken()
                if (fcmToken != null) {
                    driverRepository.checkAndRegisterDeviceToken(userId, fcmToken)
                }
            } catch (e: Exception) {
                // Silent fail - token registration is not critical for login
            }
        }
    }
}
