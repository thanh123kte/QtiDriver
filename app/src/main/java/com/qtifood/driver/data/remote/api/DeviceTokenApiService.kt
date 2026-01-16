package com.qtifood.driver.data.remote.api

import com.qtifood.driver.data.remote.dto.DeviceTokenRequest
import com.qtifood.driver.data.remote.dto.DeviceTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface DeviceTokenApiService {
    
    @GET("/api/device-tokens")
    suspend fun getDeviceTokens(@Header("X-User-ID") userId: String): Response<List<DeviceTokenResponse>>
    
    @POST("/api/device-tokens")
    suspend fun registerDeviceToken(
        @Header("X-User-ID") userId: String,
        @Body request: DeviceTokenRequest
    ): Response<DeviceTokenResponse>
}
