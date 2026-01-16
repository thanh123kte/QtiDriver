package com.qtifood.driver.data.remote.api

import com.qtifood.driver.data.remote.dto.DriverDto
import com.qtifood.driver.data.remote.dto.ImageUploadResponse
import com.qtifood.driver.data.remote.dto.OrderDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DriverApiService {
    
    // Driver endpoints
    @POST("/api/drivers")
    suspend fun createDriver(@Body driver: DriverDto): Response<DriverDto>
    
    @GET("/api/drivers/{id}")
    suspend fun getDriverById(@Path("id") id: String): Response<DriverDto>
    
    @PUT("/api/drivers/{id}")
    suspend fun updateDriver(
        @Path("id") id: String,
        @Body driver: DriverDto
    ): Response<DriverDto>
    
    // Update driver status
    @PATCH("/api/drivers/{id}/status")
    suspend fun updateDriverStatus(
        @Path("id") id: String,
        @Query("status") status: String
    ): Response<DriverDto>
    
    // Upload image with type
    @Multipart
    @POST("/api/drivers/{id}/upload-image")
    suspend fun uploadImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("imageType") imageType: RequestBody
    ): Response<ImageUploadResponse>
}
