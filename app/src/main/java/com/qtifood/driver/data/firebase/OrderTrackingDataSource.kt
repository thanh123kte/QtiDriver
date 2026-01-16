package com.qtifood.driver.data.firebase

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qtifood.driver.core.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class OrderTrackingDataSource {
    
    companion object {
        private const val TAG = "OrderTrackingDataSource"
        private const val ORDER_TRACKING_PATH = "order_tracking"
    }
    
    private val database = FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL)
    
    /**
     * Listen to order tracking updates for a specific driver
     */
    fun listenToDriverOrders(driverId: String): Flow<OrderTrackingEvent> = callbackFlow {
        val trackingRef = database.getReference(ORDER_TRACKING_PATH)
        
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val trackingData = snapshotToOrderTrackingData(snapshot)
                if (trackingData?.driverId == driverId && trackingData.status == "SHIPPING") {
                    trySend(OrderTrackingEvent.NewOrder(trackingData))
                }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val trackingData = snapshotToOrderTrackingData(snapshot)
                if (trackingData?.driverId == driverId) {
                    trySend(OrderTrackingEvent.OrderUpdated(trackingData))
                }
            }
            
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val orderId = snapshot.key
                orderId?.let {
                    trySend(OrderTrackingEvent.OrderRemoved(it))
                }
            }
            
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Not used
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error listening to orders: ${error.message}")
                trySend(OrderTrackingEvent.Error(error.message))
            }
        }
        
        trackingRef.addChildEventListener(listener)
        
        awaitClose {
            trackingRef.removeEventListener(listener)
        }
    }
    
    /**
     * Update driver location for an order
     */
    fun updateDriverLocation(orderId: Long, latitude: Double, longitude: Double) {
        val locationRef = database.getReference(ORDER_TRACKING_PATH)
            .child(orderId.toString())
            .child("driverLocation")
        
        val locationData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "updatedAt" to System.currentTimeMillis()
        )
        
        locationRef.updateChildren(locationData)
            .addOnSuccessListener {
                Log.d(TAG, "Driver location updated for order $orderId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update driver location for order $orderId", e)
            }
    }
    
    /**
     * Update order status
     */
    fun updateOrderStatus(orderId: Long, status: String) {
        val statusRef = database.getReference(ORDER_TRACKING_PATH)
            .child(orderId.toString())
            .child("status")
        
        statusRef.setValue(status)
            .addOnSuccessListener {
                Log.d(TAG, "Order status updated: $orderId -> $status")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update order status for $orderId", e)
            }
    }

    suspend fun getCurrentOrderForDriver(driverId: String): OrderTrackingData? {
        return suspendCancellableCoroutine { continuation ->
            val ref = database.getReference(ORDER_TRACKING_PATH)
            ref.get()
                .addOnSuccessListener { snapshot ->
                    var found: OrderTrackingData? = null
                    snapshot.children.forEach { child ->
                        val data = snapshotToOrderTrackingData(child)
                        if (data?.driverId == driverId &&
                            data.orderId != 0L &&
                            data.status.uppercase() != "DELIVERED" &&
                            data.status.uppercase() != "CANCELLED"
                        ) {
                            found = data
                            return@forEach
                        }
                    }
                    continuation.resume(found)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch current order for driver $driverId", e)
                    continuation.resume(null)
                }
        }
    }
    
    private fun snapshotToOrderTrackingData(snapshot: DataSnapshot): OrderTrackingData? {
        return try {
            val orderId = snapshot.child("orderId").getValue(Long::class.java)
                ?: snapshot.key?.toLongOrNull()
                ?: 0L
            
            val driverId = snapshot.child("driverId").getValue(String::class.java)
            val customerId = snapshot.child("customerId").value?.toString().orEmpty()
            val status = snapshot.child("status").getValue(String::class.java).orEmpty()
            val shippingAddressId = snapshot.child("shippingAddressId").value?.toString()?.toIntOrNull()
            val shippingAddress = snapshot.child("shippingAddress").getValue(String::class.java)
            val storeAddress = snapshot.child("storeAddress").getValue(String::class.java)
            val customerName = snapshot.child("customerName").getValue(String::class.java)
            val customerPhone = snapshot.child("customerPhone").getValue(String::class.java)
            val assignedAt = snapshot.child("assignedAt").value?.toString()
            val createdAt = snapshot.child("createdAt").value?.toString()
            val updatedAt = snapshot.child("updatedAt").value?.toString()
            
            val locationNode = snapshot.child("driverLocation")
            val driverLocation = if (locationNode.exists()) {
                DriverLocationData(
                    latitude = locationNode.child("latitude").getValue(Double::class.java) ?: 0.0,
                    longitude = locationNode.child("longitude").getValue(Double::class.java) ?: 0.0,
                    updatedAt = locationNode.child("updatedAt").value?.toString()
                )
            } else null
            
            OrderTrackingData(
                orderId = orderId,
                driverId = driverId,
                customerId = customerId,
                status = status,
                shippingAddressId = shippingAddressId,
                shippingAddress = shippingAddress,
                storeAddress = storeAddress,
                customerName = customerName,
                customerPhone = customerPhone,
                assignedAt = assignedAt,
                driverLocation = driverLocation,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse tracking snapshot: ${e.message}")
            null
        }
    }
}

/**
 * Order tracking data model
 */
data class OrderTrackingData(
    val orderId: Long = 0,
    val driverId: String? = null,
    val customerId: String = "",
    val status: String = "",
    val shippingAddressId: Int? = null,
    val shippingAddress: String? = null,
    val storeAddress: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val assignedAt: String? = null,
    val driverLocation: DriverLocationData? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class DriverLocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val updatedAt: String? = null
)

/**
 * Order tracking events
 */
sealed class OrderTrackingEvent {
    data class NewOrder(val data: OrderTrackingData) : OrderTrackingEvent()
    data class OrderUpdated(val data: OrderTrackingData) : OrderTrackingEvent()
    data class OrderRemoved(val orderId: String) : OrderTrackingEvent()
    data class Error(val message: String) : OrderTrackingEvent()
}
