package com.qtifood.driver.data.remote.api

import com.qtifood.driver.data.remote.dto.TopUpRequest
import com.qtifood.driver.data.remote.dto.TopUpResponse
import com.qtifood.driver.data.remote.dto.TransactionDto
import com.qtifood.driver.data.remote.dto.WalletDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WalletApiService {
    
    @GET("/api/wallets/{userId}")
    suspend fun getWallet(@Path("userId") userId: String): Response<WalletDto>
    
    @GET("/api/wallets/{userId}/transactions")
    suspend fun getTransactions(@Path("userId") userId: String): Response<List<TransactionDto>>
    
    @POST("/api/sepay/topup/{userId}")
    suspend fun topUp(
        @Path("userId") userId: String,
        @Body request: TopUpRequest
    ): Response<TopUpResponse>
}
