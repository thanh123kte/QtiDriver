package com.qtifood.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("details")
    val details: String? = null
)
