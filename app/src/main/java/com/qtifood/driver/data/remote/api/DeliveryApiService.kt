package com.qtifood.driver.data.remote.api

import com.qtifood.driver.data.remote.dto.DeliveryDto
import com.qtifood.driver.data.remote.dto.DeliveryIncomeDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeliveryApiService {
    @GET("/api/deliveries/driver/{driverId}")
    suspend fun getDeliveriesByDriver(
        @Path("driverId") driverId: String
    ): Response<List<DeliveryDto>>

    @GET("/api/deliveries/driver/{driverId}/income-stats")
    suspend fun getIncomeStats(
        @Path("driverId") driverId: String,
        @Query("period") period: String
    ): Response<DeliveryIncomeDto>
}
