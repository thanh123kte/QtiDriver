package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AddressDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("receiver")
    val receiver: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("isDefault")
    val isDefault: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)
