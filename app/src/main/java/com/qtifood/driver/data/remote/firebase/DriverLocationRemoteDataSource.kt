package com.qtifood.driver.data.remote.firebase

import com.google.firebase.database.FirebaseDatabase
import com.qtifood.driver.core.Constants
import com.qtifood.driver.domain.model.DriverLocation
import kotlinx.coroutines.tasks.await

class DriverLocationRemoteDataSource {
    
    private val database = FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL)
    private val locationsRef = database.getReference("driver_locations")
    
    suspend fun updateDriverLocation(location: DriverLocation) {
        val locationMap = mapOf(
            "driverId" to location.driverId,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to location.timestamp,
            "isOnline" to location.isOnline
        )
        
        locationsRef.child(location.driverId).setValue(locationMap).await()
    }
    
    suspend fun getDriverLocation(driverId: String): DriverLocation? {
        val snapshot = locationsRef.child(driverId).get().await()
        
        return if (snapshot.exists()) {
            DriverLocation(
                driverId = snapshot.child("driverId").getValue(String::class.java) ?: "",
                latitude = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0,
                longitude = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0,
                timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
                isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
            )
        } else {
            null
        }
    }
}
