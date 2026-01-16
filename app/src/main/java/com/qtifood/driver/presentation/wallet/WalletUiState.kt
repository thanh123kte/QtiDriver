package com.qtifood.driver.presentation.wallet

import com.qtifood.driver.domain.model.TopUp
import com.qtifood.driver.domain.model.Transaction
import com.qtifood.driver.domain.model.Wallet

data class WalletUiState(
    val wallet: Wallet? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val topUpResult: TopUp? = null
)
