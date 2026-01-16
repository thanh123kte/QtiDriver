package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("fullName")
    val fullName: String?,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("vehicleType")
    val vehicleType: String? = null,
    
    @SerializedName("vehiclePlate")
    val vehiclePlate: String? = null,
    
    @SerializedName("cccdNumber")
    val cccdNumber: String,
    
    @SerializedName("cccdFrontImageUrl")
    val cccdFrontImageUrl: String? = null,
    
    @SerializedName("cccdBackImageUrl")
    val cccdBackImageUrl: String? = null,
    
    @SerializedName("licenseNumber")
    val licenseNumber: String,
    
    @SerializedName("licenseImageUrl")
    val licenseImageUrl: String? = null,
    
    @SerializedName("vehicleRegistrationImageUrl")
    val vehicleRegistrationImageUrl: String? = null,
    
    @SerializedName("vehiclePlateImageUrl")
    val vehiclePlateImageUrl: String? = null,
    
    @SerializedName("verified")
    val verified: Boolean = false,
    
    @SerializedName("verificationStatus")
    val verificationStatus: String,

    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
