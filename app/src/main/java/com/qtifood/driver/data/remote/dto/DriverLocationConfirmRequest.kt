package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverLocationConfirmRequest(
    @SerializedName("orderId")
    val orderId: Int,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)
