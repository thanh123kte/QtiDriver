package com.qtifood.driver.domain.repository

import com.google.firebase.auth.PhoneAuthCredential
import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.DriverLocation
import com.qtifood.driver.domain.model.Order
import com.qtifood.driver.domain.model.Result

interface DriverRepository {
    
    /**
     * Firebase Authentication
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<String>
    
    fun getCurrentFirebaseUid(): String?
    
    fun signOut()
    
    /**
     * Driver Management
     */
    suspend fun getDriverByFirebaseUid(firebaseUid: String): Result<Driver?>
    
    suspend fun createDriver(driver: Driver): Result<Driver>
    
    suspend fun updateDriver(driver: Driver): Result<Driver>
    
    suspend fun getDriverProfile(driverId: String): Result<Driver>
    
    suspend fun uploadAvatar(driverId: String, imageUri: String): Result<Driver>
    
    suspend fun uploadLicense(driverId: String, imageUri: String): Result<Driver>
    
    suspend fun uploadCccdFront(driverId: String, imageUri: String): Result<Driver>
    
    suspend fun uploadCccdBack(driverId: String, imageUri: String): Result<Driver>
    
    suspend fun uploadVehicleRegistration(driverId: String, imageUri: String): Result<Driver>
    
    suspend fun uploadVehiclePlate(driverId: String, imageUri: String): Result<Driver>
    
    /**
     * Driver Status Management
     */
    suspend fun updateDriverStatus(driverId: String, status: String): Result<Driver>
    
    /**
     * Location Management
     */
    suspend fun updateDriverLocation(location: DriverLocation): Result<Unit>
    
    suspend fun getDriverLocation(driverId: String): Result<DriverLocation>
    
    /**
     * Device Token Management
     */
    suspend fun checkAndRegisterDeviceToken(userId: String, token: String): Result<Boolean>
}
