package com.qtifood.driver.data.mapper

import com.qtifood.driver.data.remote.dto.DeliveryDto
import com.qtifood.driver.data.remote.dto.DeliveryIncomeDto
import com.qtifood.driver.domain.model.Delivery
import com.qtifood.driver.domain.model.DeliveryIncome

object DeliveryMapper {
    fun toDomain(dto: DeliveryDto): Delivery {
        return Delivery(
            id = dto.id,
            orderId = dto.orderId,
            driverId = dto.driverId,
            driverName = dto.driverName,
            distanceKm = dto.distanceKm,
            goodsAmount = dto.goodsAmount,
            shippingFee = dto.shippingFee,
            driverIncome = dto.driverIncome,
            paymentMethod = dto.paymentMethod,
            storeName = dto.storeName,
            shippingAddress = dto.shippingAddress,
            customerName = dto.customerName,
            status = dto.status,
            startedAt = dto.startedAt,
            completedAt = dto.completedAt
        )
    }

    fun toDomain(dto: DeliveryIncomeDto): DeliveryIncome {
        return DeliveryIncome(
            period = dto.period,
            startDate = dto.startDate,
            endDate = dto.endDate,
            totalDeliveries = dto.totalDeliveries,
            totalIncome = dto.totalIncome,
            totalShippingFee = dto.totalShippingFee,
            totalDistance = dto.totalDistance,
            averageIncomePerDelivery = dto.averageIncomePerDelivery,
            averageDistancePerDelivery = dto.averageDistancePerDelivery
        )
    }
}
