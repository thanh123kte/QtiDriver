package com.qtifood.driver.data.repository

import android.util.Log
import com.qtifood.driver.data.mapper.WalletMapper
import com.qtifood.driver.data.remote.api.WalletApiService
import com.qtifood.driver.data.remote.dto.TopUpRequest
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.model.TopUp
import com.qtifood.driver.domain.model.Transaction
import com.qtifood.driver.domain.model.Wallet
import com.qtifood.driver.domain.repository.WalletRepository

class WalletRepositoryImpl(
    private val apiService: WalletApiService
) : WalletRepository {
    
    companion object {
        private const val TAG = "WalletRepositoryImpl"
    }
    
    override suspend fun getWallet(userId: String): Result<Wallet> {
        return try {
            Log.d(TAG, "Fetching wallet for userId: $userId")
            val response = apiService.getWallet(userId)
            
            if (response.isSuccessful) {
                val walletDto = response.body()
                if (walletDto != null) {
                    val wallet = WalletMapper.toDomain(walletDto)
                    Log.d(TAG, "Wallet fetched successfully: balance=${wallet.balance}")
                    Result.Success(wallet)
                } else {
                    Log.e(TAG, "Response body is null")
                    Result.Error("Failed to fetch wallet: empty response")
                }
            } else {
                val errorMsg = "Failed to fetch wallet: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching wallet", e)
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    override suspend fun getTransactions(userId: String): Result<List<Transaction>> {
        return try {
            Log.d(TAG, "Fetching transactions for userId: $userId")
            val response = apiService.getTransactions(userId)
            
            if (response.isSuccessful) {
                val transactionDtos = response.body()
                if (transactionDtos != null) {
                    val transactions = transactionDtos.map { WalletMapper.toDomain(it) }
                    Log.d(TAG, "Transactions fetched successfully: ${transactions.size} items")
                    Result.Success(transactions)
                } else {
                    Log.d(TAG, "No transactions found")
                    Result.Success(emptyList())
                }
            } else {
                val errorMsg = "Failed to fetch transactions: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching transactions", e)
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    override suspend fun topUp(userId: String, amount: Double): Result<TopUp> {
        return try {
            Log.d(TAG, "Top up for userId: $userId, amount: $amount")
            val request = TopUpRequest(amount)
            val response = apiService.topUp(userId, request)
            
            if (response.isSuccessful) {
                val topUpDto = response.body()
                if (topUpDto != null) {
                    val topUp = WalletMapper.toDomain(topUpDto)
                    Log.d(TAG, "Top up successful: ${topUp.providerTransactionId}")
                    Result.Success(topUp)
                } else {
                    Log.e(TAG, "Response body is null")
                    Result.Error("Failed to top up: empty response")
                }
            } else {
                val errorMsg = "Failed to top up: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during top up", e)
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
}
