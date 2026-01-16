package com.qtifood.driver.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.qtifood.driver.R
import com.qtifood.driver.domain.model.DriverLocation
import com.qtifood.driver.domain.usecase.UpdateDriverLocationUseCase
import com.qtifood.driver.presentation.home.DriverHomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground service để tracking vị trí tài xế liên tục
 * Hoạt động ngay cả khi app ở background
 */
class LocationTrackingService : Service() {

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 12345
        private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
        private const val FASTEST_LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
        
        const val ACTION_START = "ACTION_START_LOCATION_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_LOCATION_TRACKING"
        const val EXTRA_DRIVER_ID = "EXTRA_DRIVER_ID"
        
        fun start(context: Context, driverId: String) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DRIVER_ID, driverId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val updateLocationUseCase: UpdateDriverLocationUseCase by inject()
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var driverId: String? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== LocationTrackingService onCreate ===")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "=== onStartCommand: action=${intent?.action} ===")
        
        when (intent?.action) {
            ACTION_START -> {
                driverId = intent.getStringExtra(EXTRA_DRIVER_ID)
                if (driverId != null) {
                    Log.d(TAG, "Starting location tracking for driver: $driverId")
                    startForeground(NOTIFICATION_ID, createNotification())
                    startLocationUpdates()
                } else {
                    Log.e(TAG, "Driver ID is null, stopping service")
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                Log.d(TAG, "Stopping location tracking")
                stopLocationUpdates()
                stopSelf()
            }
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted")
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        )
            .setMinUpdateIntervalMillis(FASTEST_LOCATION_UPDATE_INTERVAL)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d(TAG, "Location update: lat=${location.latitude}, lng=${location.longitude}")
                    updateLocationToFirebase(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        Log.d(TAG, "Location updates started")
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Location updates stopped")
        }
    }

    private fun updateLocationToFirebase(latitude: Double, longitude: Double) {
        val currentDriverId = driverId ?: return
        
        serviceScope.launch {
            try {
                val location = DriverLocation(
                    driverId = currentDriverId,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis(),
                    isOnline = true
                )
                
                updateLocationUseCase(location)
                Log.d(TAG, "Location updated to Firebase: $latitude, $longitude")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update location to Firebase", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Theo dõi vị trí tài xế"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, DriverHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QTI Driver")
            .setContentText("Đang theo dõi vị trí của bạn")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        Log.d(TAG, "=== LocationTrackingService onDestroy ===")
        stopLocationUpdates()
        serviceScope.cancel()
        super.onDestroy()
    }
}
