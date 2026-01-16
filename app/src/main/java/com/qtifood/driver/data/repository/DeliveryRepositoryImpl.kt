package com.qtifood.driver.data.repository

import android.util.Log
import com.qtifood.driver.data.mapper.DeliveryMapper
import com.qtifood.driver.data.remote.api.DeliveryApiService
import com.qtifood.driver.domain.model.Delivery
import com.qtifood.driver.domain.model.DeliveryIncome
import com.qtifood.driver.domain.repository.DeliveryRepository

class DeliveryRepositoryImpl(
    private val apiService: DeliveryApiService
) : DeliveryRepository {

    companion object {
        private const val TAG = "DeliveryRepository"
    }

    override suspend fun getDeliveriesByDriver(driverId: String): Result<List<Delivery>> {
        return try {
            val response = apiService.getDeliveriesByDriver(driverId)
            if (response.isSuccessful && response.body() != null) {
                val deliveries = response.body()!!.map { DeliveryMapper.toDomain(it) }
                Result.success(deliveries)
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch deliveries", e)
            Result.failure(e)
        }
    }

    override suspend fun getIncomeStats(driverId: String, period: String): Result<DeliveryIncome> {
        return try {
            val response = apiService.getIncomeStats(driverId, period)
            if (response.isSuccessful && response.body() != null) {
                Result.success(DeliveryMapper.toDomain(response.body()!!))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch income stats", e)
            Result.failure(e)
        }
    }
}
