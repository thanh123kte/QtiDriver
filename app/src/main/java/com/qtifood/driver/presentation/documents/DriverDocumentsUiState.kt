package com.qtifood.driver.presentation.documents

import com.qtifood.driver.domain.model.Driver

data class DriverDocumentsUiState(
    val isLoading: Boolean = false,
    val driver: Driver? = null,
    val errorMessage: String? = null,
    val isUpdateSuccess: Boolean = false
)
