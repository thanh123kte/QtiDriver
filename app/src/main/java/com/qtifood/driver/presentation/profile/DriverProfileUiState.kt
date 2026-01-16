package com.qtifood.driver.presentation.profile

import com.qtifood.driver.domain.model.Driver

data class DriverProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val driver: Driver? = null,
    val isEditMode: Boolean = false,
    val isUpdateSuccess: Boolean = false,
    val avatarUri: String? = null,
    val approvalStatusText: String = "Đang chờ duyệt"
)
