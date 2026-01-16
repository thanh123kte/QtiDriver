package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("customerName")
    val customerName: String,
    
    @SerializedName("customerPhone")
    val customerPhone: String,
    
    @SerializedName("pickupAddress")
    val pickupAddress: String,
    
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    
    @SerializedName("totalAmount")
    val totalAmount: Double,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("createdAt")
    val createdAt: Long
)
