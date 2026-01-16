package com.qtifood.driver.data.mapper

import com.qtifood.driver.data.remote.dto.DriverDto
import com.qtifood.driver.data.remote.dto.OrderDto
import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.Order
import com.qtifood.driver.domain.model.OrderStatus
import com.qtifood.driver.domain.model.VerificationStatus

object DriverMapper {
    
    fun toDomain(dto: DriverDto): Driver {
        return Driver(
            id = dto.id,
            fullName = dto.fullName,
            phone = dto.phone,
            avatarUrl = dto.avatarUrl,
            dateOfBirth = dto.dateOfBirth,
            address = dto.address,
            email = dto.email,
            vehicleType = dto.vehicleType,
            vehiclePlate = dto.vehiclePlate,
            cccdNumber = dto.cccdNumber,
            cccdFrontImageUrl = dto.cccdFrontImageUrl,
            cccdBackImageUrl = dto.cccdBackImageUrl,
            licenseNumber = dto.licenseNumber,
            licenseImageUrl = dto.licenseImageUrl,
            vehicleRegistrationImageUrl = dto.vehicleRegistrationImageUrl,
            vehiclePlateImageUrl = dto.vehiclePlateImageUrl,
            verified = dto.verified,
            verificationStatus = mapVerificationStatus(dto.verificationStatus),
            status = dto.status,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
    
    fun toDto(domain: Driver): DriverDto {
        return DriverDto(
            id = domain.id,
            fullName = domain.fullName,
            phone = domain.phone,
            avatarUrl = domain.avatarUrl,
            dateOfBirth = domain.dateOfBirth,
            address = domain.address,
            email = domain.email,
            vehicleType = domain.vehicleType,
            vehiclePlate = domain.vehiclePlate,
            cccdNumber = domain.cccdNumber,
            cccdFrontImageUrl = domain.cccdFrontImageUrl,
            cccdBackImageUrl = domain.cccdBackImageUrl,
            licenseNumber = domain.licenseNumber,
            licenseImageUrl = domain.licenseImageUrl,
            vehicleRegistrationImageUrl = domain.vehicleRegistrationImageUrl,
            vehiclePlateImageUrl = domain.vehiclePlateImageUrl,
            verified = domain.verified,
            verificationStatus = domain.verificationStatus.name,
            status = domain.status,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
    
    fun orderToDomain(dto: OrderDto): Order {
        return Order(
            id = dto.id,
            customerName = dto.customerName,
            customerPhone = dto.customerPhone,
            pickupAddress = dto.pickupAddress,
            deliveryAddress = dto.deliveryAddress,
            totalAmount = dto.totalAmount,
            status = mapOrderStatus(dto.status),
            createdAt = dto.createdAt
        )
    }
    
    private fun mapVerificationStatus(status: String): VerificationStatus {
        return try {
            VerificationStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            VerificationStatus.PENDING
        }
    }
    
    private fun mapOrderStatus(status: String): OrderStatus {
        return try {
            OrderStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            OrderStatus.PENDING
        }
    }
}
