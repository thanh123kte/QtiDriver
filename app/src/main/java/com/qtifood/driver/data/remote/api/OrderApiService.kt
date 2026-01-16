package com.qtifood.driver.data.remote.api

import com.qtifood.driver.data.remote.dto.OrderDetailDto
import com.qtifood.driver.data.remote.dto.OrderItemDto
import com.qtifood.driver.data.remote.dto.AddressDto
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {
    
    @GET("/api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Int): Response<OrderDetailDto>
    
    @GET("/api/order-items/order/{orderId}")
    suspend fun getOrderItems(
        @Path("orderId") orderId: Int
    ): Response<List<OrderItemDto>>
    
    @PATCH("/api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Int,
        @Query("status") status: String
    ): Response<OrderDetailDto>

    @GET("/api/addresses/{id}")
    suspend fun getAddressById(
        @Path("id") id: Int
    ): Response<AddressDto>

    @POST("/api/orders/{orderId}/driver-location-confirm")
    suspend fun confirmDriverLocation(
        @Path("orderId") orderId: Int,
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): Response<OrderDetailDto>
}
