package com.qtifood.driver.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.qtifood.driver.data.mapper.DriverMapper
import com.qtifood.driver.data.remote.api.DeviceTokenApiService
import com.qtifood.driver.data.remote.api.DriverApiService
import com.qtifood.driver.data.remote.dto.DeviceTokenRequest
import com.qtifood.driver.data.remote.firebase.DriverLocationRemoteDataSource
import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.DriverLocation
import com.qtifood.driver.domain.model.Order
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class DriverRepositoryImpl(
    private val apiService: DriverApiService,
    private val deviceTokenApiService: DeviceTokenApiService,
    private val locationDataSource: DriverLocationRemoteDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) : DriverRepository {
    
    companion object {
        private const val TAG = "DriverRepositoryImpl"
    }
    
    override suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<String> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUid = authResult.user?.uid
            if (firebaseUid != null) {
                Result.Success(firebaseUid)
            } else {
                Result.Error("Failed to get Firebase UID")
            }
        } catch (e: Exception) {
            Result.Error("Authentication failed: ${e.message}", e)
        }
    }
    
    override fun getCurrentFirebaseUid(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    override fun signOut() {
        firebaseAuth.signOut()
    }
    
    override suspend fun getDriverByFirebaseUid(firebaseUid: String): Result<Driver?> {
        return try {
            val response = apiService.getDriverById(firebaseUid)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(DriverMapper.toDomain(response.body()!!))
            } else if (response.code() == 404) {
                // Driver not found - this is OK, means new user
                Result.Success(null)
            } else {
                Result.Error("Failed to fetch driver: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}", e)
        }
    }
    
    override suspend fun createDriver(driver: Driver): Result<Driver> {
        return try {
            val driverDto = DriverMapper.toDto(driver)
            val response = apiService.createDriver(driverDto)
            
            if (response.isSuccessful && response.body() != null) {
                Result.Success(DriverMapper.toDomain(response.body()!!))
            } else {
                Result.Error("Failed to create driver: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}", e)
        }
    }
    
    override suspend fun updateDriver(driver: Driver): Result<Driver> {
        return try {
            val driverDto = DriverMapper.toDto(driver)
            val response = apiService.updateDriver(driver.id, driverDto)
            
            if (response.isSuccessful && response.body() != null) {
                Result.Success(DriverMapper.toDomain(response.body()!!))
            } else {
                Result.Error("Failed to update driver: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}", e)
        }
    }
    
    override suspend fun getDriverProfile(driverId: String): Result<Driver> {
        return try {
            val response = apiService.getDriverById(driverId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(DriverMapper.toDomain(response.body()!!))
            } else {
                Result.Error("Failed to fetch driver profile: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}", e)
        }
    }
    
    override suspend fun updateDriverLocation(location: DriverLocation): Result<Unit> {
        return try {
            locationDataSource.updateDriverLocation(location)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update location: ${e.message}", e)
        }
    }
    
    override suspend fun getDriverLocation(driverId: String): Result<DriverLocation> {
        return try {
            val location = locationDataSource.getDriverLocation(driverId)
            if (location != null) {
                Result.Success(location)
            } else {
                Result.Error("Location not found")
            }
        } catch (e: Exception) {
            Result.Error("Failed to get location: ${e.message}", e)
        }
    }
    
    override suspend fun uploadAvatar(driverId: String, imageUri: String): Result<Driver> {
        return uploadImage(driverId, imageUri, "avatar")
    }
    
    override suspend fun uploadLicense(driverId: String, imageUri: String): Result<Driver> {
        return uploadImage(driverId, imageUri, "license")
    }
    
    override suspend fun uploadCccdFront(driverId: String, imageUri: String): Result<Driver> {
        return uploadImage(driverId, imageUri, "cccd_front")
    }
    
    override suspend fun uploadCccdBack(driverId: String, imageUri: String): Result<Driver> {
        return uploadImage(driverId, imageUri, "cccd_back")
    }
    
    override suspend fun uploadVehicleRegistration(driverId: String, imageUri: String): Result<Driver> {
        return uploadImage(driverId, imageUri, "vehicle_registration")
    }
    
    override suspend fun uploadVehiclePlate(driverId: String, imageUri: String): Result<Driver> {
        return uploadImage(driverId, imageUri, "vehicle_plate")
    }
    
    private suspend fun uploadImage(driverId: String, imageUri: String, imageType: String): Result<Driver> {
        return try {
            Log.d(TAG, "uploadImage - Type: $imageType, Driver ID: $driverId, URI: $imageUri")
            val file = uriToFile(Uri.parse(imageUri))
            Log.d(TAG, "uploadImage - File created: ${file.absolutePath}, size: ${file.length()} bytes")
            
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val imageTypePart = imageType.toRequestBody("text/plain".toMediaTypeOrNull())
            
            Log.d(TAG, "uploadImage - Calling API with imageType: $imageType")
            val response = apiService.uploadImage(driverId, filePart, imageTypePart)
            
            if (response.isSuccessful && response.body() != null) {
                val imageUrl = response.body()!!.imageUrl
                Log.d(TAG, "uploadImage - Success! Image URL: $imageUrl for type: $imageType")
                
                // Fetch updated driver profile to get all image URLs
                Log.d(TAG, "uploadImage - Fetching updated driver profile...")
                when (val profileResult = getDriverProfile(driverId)) {
                    is Result.Success -> {
                        Log.d(TAG, "uploadImage - Driver profile fetched successfully")
                        Result.Success(profileResult.data)
                    }
                    is Result.Error -> {
                        Log.e(TAG, "uploadImage - Failed to fetch driver profile: ${profileResult.message}")
                        Result.Error("Upload successful but failed to fetch updated profile: ${profileResult.message}")
                    }
                    is Result.Loading -> Result.Error("Unexpected loading state")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "uploadImage - Failed: ${response.code()} ${response.message()}")
                Log.e(TAG, "uploadImage - Error body: $errorBody")
                Result.Error("Failed to upload $imageType: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage - Exception for type $imageType: ${e.message}", e)
            Result.Error("Upload error: ${e.message}", e)
        }
    }
    
    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        tempFile.deleteOnExit()
        
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
    
    override suspend fun checkAndRegisterDeviceToken(userId: String, token: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Checking device tokens for userId: $userId")
            
            // Check if token already exists
            val getResponse = deviceTokenApiService.getDeviceTokens(userId)
            
            if (getResponse.isSuccessful) {
                val existingTokens = getResponse.body() ?: emptyList()
                
                // Check if current token already registered
                val tokenExists = existingTokens.any { it.token == token }
                
                if (tokenExists) {
                    Log.d(TAG, "Device token already registered")
                    return Result.Success(true)
                }
                
                // Register new token
                Log.d(TAG, "Registering new device token")
                val request = DeviceTokenRequest(
                    token = token,
                    platform = "ANDROID"
                )
                
                val registerResponse = deviceTokenApiService.registerDeviceToken(userId, request)
                
                if (registerResponse.isSuccessful) {
                    Log.d(TAG, "Device token registered successfully")
                    Result.Success(true)
                } else {
                    val errorMsg = "Failed to register token: ${registerResponse.code()} - ${registerResponse.message()}"
                    Log.e(TAG, errorMsg)
                    Result.Error(errorMsg)
                }
            } else {
                val errorMsg = "Failed to get tokens: ${getResponse.code()} - ${getResponse.message()}"
                Log.e(TAG, errorMsg)
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking/registering device token", e)
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    override suspend fun updateDriverStatus(driverId: String, status: String): Result<Driver> {
        return try {
            Log.d(TAG, "Updating driver status: $driverId -> $status")
            val response = apiService.updateDriverStatus(driverId, status)
            
            if (response.isSuccessful && response.body() != null) {
                val driver = DriverMapper.toDomain(response.body()!!)
                Log.d(TAG, "Driver status updated successfully")
                Result.Success(driver)
            } else {
                val errorMsg = "Failed to update status: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating driver status", e)
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
}

