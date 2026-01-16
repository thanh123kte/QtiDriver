package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderDetailDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("customerId")
    val customerId: String,
    
    @SerializedName("storeId")
    val storeId: Int,
    
    @SerializedName("driverId")
    val driverId: String?,
    
    @SerializedName("shippingAddressId")
    val shippingAddressId: Int,
    
    @SerializedName("totalAmount")
    val totalAmount: Double,
    
    @SerializedName("shippingFee")
    val shippingFee: Double,
    
    @SerializedName("adminVoucherId")
    val adminVoucherId: Int?,
    
    @SerializedName("sellerVoucherId")
    val sellerVoucherId: Int?,
    
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    
    @SerializedName("paymentStatus")
    val paymentStatus: String,
    
    @SerializedName("paidAt")
    val paidAt: String?,
    
    @SerializedName("orderStatus")
    val orderStatus: String,
    
    @SerializedName("note")
    val note: String?,
    
    @SerializedName("cancelReason")
    val cancelReason: String?,
    
    @SerializedName("expectedDeliveryTime")
    val expectedDeliveryTime: String?,
    
    @SerializedName("ratingStatus")
    val ratingStatus: Boolean,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class OrderItemDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("orderId")
    val orderId: Int,
    
    @SerializedName("productId")
    val productId: Int,
    
    @SerializedName("productName")
    val productName: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("totalPrice")
    val totalPrice: Double
)
