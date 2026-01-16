package com.qtifood.driver.domain.model

data class Driver(
    val id: String,
    val fullName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val dateOfBirth: String? = null,
    val address: String? = null,
    val email: String? = null,
    val vehicleType: String? = null,
    val vehiclePlate: String? = null,
    val cccdNumber: String,
    val cccdFrontImageUrl: String? = null,
    val cccdBackImageUrl: String? = null,
    val licenseNumber: String,
    val licenseImageUrl: String? = null,
    val vehicleRegistrationImageUrl: String? = null,
    val vehiclePlateImageUrl: String? = null,
    val verified: Boolean = false,
    val verificationStatus: VerificationStatus,
    val status: String? = null, // OFFLINE, ONLINE, BUSY
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
