package com.qtifood.driver.domain.repository

import com.qtifood.driver.data.remote.dto.OrderDetailDto
import com.qtifood.driver.data.remote.dto.OrderItemDto
import com.qtifood.driver.data.remote.dto.AddressDto

interface OrderRepository {
    suspend fun getOrderById(orderId: Int): Result<OrderDetailDto>
    suspend fun getOrderItems(orderId: Int): Result<List<OrderItemDto>>
    suspend fun updateOrderStatus(orderId: Int, status: String): Result<OrderDetailDto>
    suspend fun getAddressById(addressId: Int): Result<AddressDto>
    suspend fun confirmDriverLocation(orderId: Int, latitude: Double, longitude: Double): Result<OrderDetailDto>
}
