package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeviceTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String
)

data class DeviceTokenResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("userId") val userId: String,
    @SerializedName("role") val role: String,
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)
