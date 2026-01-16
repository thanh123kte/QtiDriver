package com.qtifood.driver.data.mapper

import com.qtifood.driver.data.remote.dto.TopUpResponse
import com.qtifood.driver.data.remote.dto.TransactionDto
import com.qtifood.driver.data.remote.dto.WalletDto
import com.qtifood.driver.domain.model.TopUp
import com.qtifood.driver.domain.model.Transaction
import com.qtifood.driver.domain.model.TransactionStatus
import com.qtifood.driver.domain.model.TransactionType
import com.qtifood.driver.domain.model.Wallet

object WalletMapper {
    
    fun toDomain(dto: WalletDto): Wallet {
        return Wallet(
            id = dto.id,
            userId = dto.userId,
            balance = dto.balance,
            totalDeposited = dto.totalDeposited,
            totalWithdrawn = dto.totalWithdrawn,
            totalEarned = dto.totalEarned,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
    
    fun toDomain(dto: TransactionDto): Transaction {
        return Transaction(
            id = dto.id,
            walletId = dto.walletId,
            amount = dto.amount,
            type = mapTransactionType(dto.type),
            status = mapTransactionStatus(dto.status),
            balanceBefore = dto.balanceBefore,
            balanceAfter = dto.balanceAfter,
            description = dto.description,
            referenceId = dto.referenceId,
            referenceType = dto.referenceType,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
    
    fun toDomain(dto: TopUpResponse): TopUp {
        return TopUp(
            id = dto.id,
            amount = dto.amount,
            providerTransactionId = dto.providerTransactionId,
            status = dto.status,
            createdAt = dto.createdAt
        )
    }
    
    private fun mapTransactionType(type: String): TransactionType {
        return try {
            TransactionType.valueOf(type.uppercase())
        } catch (e: Exception) {
            TransactionType.PAYMENT
        }
    }
    
    private fun mapTransactionStatus(status: String): TransactionStatus {
        return try {
            TransactionStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            TransactionStatus.PENDING
        }
    }
}
