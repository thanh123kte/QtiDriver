package com.qtifood.driver.presentation.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val userId: String
) : ViewModel() {
    
    companion object {
        private const val TAG = "WalletViewModel"
    }
    
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()
    
    init {
        loadWalletData()
    }
    
    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Load wallet
            when (val walletResult = walletRepository.getWallet(userId)) {
                is Result.Success -> {
                    Log.d(TAG, "Wallet loaded: ${walletResult.data}")
                    _uiState.update { it.copy(wallet = walletResult.data) }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error loading wallet: ${walletResult.message}")
                    _uiState.update { it.copy(errorMessage = walletResult.message) }
                }
                else -> {
                    // Handle other cases if needed
                }
            }
            
            // Load transactions
            when (val transactionsResult = walletRepository.getTransactions(userId)) {
                is Result.Success -> {
                    Log.d(TAG, "Transactions loaded: ${transactionsResult.data.size} items")
                    _uiState.update { it.copy(transactions = transactionsResult.data) }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error loading transactions: ${transactionsResult.message}")
                    if (_uiState.value.errorMessage == null) {
                        _uiState.update { it.copy(errorMessage = transactionsResult.message) }
                    }
                }
                else -> {
                    // Handle other cases if needed
                }
            }
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun topUp(amount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = walletRepository.topUp(userId, amount)) {
                is Result.Success -> {
                    Log.d(TAG, "Top up successful: ${result.data.providerTransactionId}")
                    _uiState.update { it.copy(topUpResult = result.data, isLoading = false) }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error during top up: ${result.message}")
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
    
    fun clearTopUpResult() {
        _uiState.update { it.copy(topUpResult = null) }
    }
}
