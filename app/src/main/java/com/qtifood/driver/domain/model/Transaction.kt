package com.qtifood.driver.domain.model

data class Transaction(
    val id: Long,
    val walletId: Long,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val description: String?,
    val referenceId: String?,
    val referenceType: String?,
    val createdAt: String,
    val updatedAt: String? = null
)

enum class TransactionType {
    DEPOSIT,         // Nap tien
    WITHDRAW,        // Rut tien
    PAYMENT,         // Thanh toan
    REFUND,          // Hoan tien
    EARN,         // Thu nhap chung
    DELIVERY_INCOME,  // Thu nhap giao hang

    MANUAL_INCOME // Thu nhap cod dong


}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
