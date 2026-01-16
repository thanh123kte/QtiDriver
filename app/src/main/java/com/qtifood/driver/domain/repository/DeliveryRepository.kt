package com.qtifood.driver.domain.repository

import com.qtifood.driver.domain.model.Delivery
import com.qtifood.driver.domain.model.DeliveryIncome

interface DeliveryRepository {
    suspend fun getDeliveriesByDriver(driverId: String): Result<List<Delivery>>
    suspend fun getIncomeStats(driverId: String, period: String): Result<DeliveryIncome>
}
