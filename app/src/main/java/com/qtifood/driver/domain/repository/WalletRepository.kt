package com.qtifood.driver.domain.repository

import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.model.TopUp
import com.qtifood.driver.domain.model.Transaction
import com.qtifood.driver.domain.model.Wallet

interface WalletRepository {
    suspend fun getWallet(userId: String): Result<Wallet>
    suspend fun getTransactions(userId: String): Result<List<Transaction>>
    suspend fun topUp(userId: String, amount: Double): Result<TopUp>
}
