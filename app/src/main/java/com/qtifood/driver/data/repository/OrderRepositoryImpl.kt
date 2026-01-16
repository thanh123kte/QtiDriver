package com.qtifood.driver.data.repository

import android.util.Log
import com.qtifood.driver.core.Constants
import com.qtifood.driver.data.remote.api.OrderApiService
import com.qtifood.driver.data.remote.dto.AddressDto
import com.qtifood.driver.data.remote.dto.OrderDetailDto
import com.qtifood.driver.data.remote.dto.OrderItemDto
import com.qtifood.driver.data.remote.util.ErrorHandler
import com.qtifood.driver.domain.repository.OrderRepository

class OrderRepositoryImpl(
    private val apiService: OrderApiService
) : OrderRepository {
    
    companion object {
        private const val TAG = "OrderRepository"
    }
    
    override suspend fun getOrderById(orderId: Int): Result<OrderDetailDto> {
        logUrl("/api/orders/$orderId")
        return try {
            val response = apiService.getOrderById(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorHandler.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOrderItems(orderId: Int): Result<List<OrderItemDto>> {
        logUrl("/api/order-items/order/$orderId")
        return try {
            val response = apiService.getOrderItems(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorHandler.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateOrderStatus(orderId: Int, status: String): Result<OrderDetailDto> {
        logUrl("/api/orders/$orderId/status?status=$status")
        return try {
            val response = apiService.updateOrderStatus(orderId, status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorHandler.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAddressById(addressId: Int): Result<AddressDto> {
        logUrl("/api/addresses/$addressId")
        return try {
            val response = apiService.getAddressById(addressId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorHandler.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmDriverLocation(orderId: Int, latitude: Double, longitude: Double): Result<OrderDetailDto> {
        logUrl("/api/orders/$orderId/driver-location-confirm?lat=$latitude&lng=$longitude")
        return try {
            val response = apiService.confirmDriverLocation(orderId, latitude, longitude)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorHandler.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun logUrl(path: String) {
        val full = "${Constants.BASE_URL}$path"
        Log.d(TAG, "Calling: $full")
    }
}
