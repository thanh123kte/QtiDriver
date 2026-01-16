package com.qtifood.driver.data.firebase

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.qtifood.driver.R
import com.qtifood.driver.presentation.common.NotificationPopup
import com.qtifood.driver.presentation.home.DriverHomeActivity
import com.qtifood.driver.presentation.order.OrderDetailActivity

class DriverFCMService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "DriverFCMService"
        private const val CHANNEL_ID = "order_updates"
        private const val CHANNEL_NAME = "Thong bao QTI Driver"
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: $token")
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "QTI Driver"
        
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Ban co thong bao moi"
        
        val notificationType = message.data["notificationType"] ?: message.data["type"]
        val orderId = message.data["orderId"] ?: message.data["order_id"]
        
        Log.d(TAG, "FCM received - type: $notificationType, orderId: $orderId, data: ${message.data}")
        
        val isForeground = isAppInForeground()
        val isDeliveryNotification = notificationType.equals("DELIVERY", true) ||
                notificationType.equals("ORDER", true)
        
        if (isDeliveryNotification) {
            showOrderPopup(title, body, orderId)
        } else if (isForeground) {
            NotificationPopup.showGenericNotification(applicationContext, title, body)
        }
        
        if (!isForeground) {
            showNotification(title, body, orderId)
        }
        
        playNotificationSound()
    }
    
    private fun showOrderPopup(title: String, body: String, orderId: String?) {
        Handler(Looper.getMainLooper()).post {
            if (orderId != null && canDrawOverlays()) {
                NotificationPopup.showOrderNotification(
                    applicationContext,
                    title,
                    body,
                    orderId
                )
            } else {
                NotificationPopup.showGenericNotification(applicationContext, title, body)
            }
        }
    }
    
    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }
    
    private fun playNotificationSound() {
        try {
            val mediaPlayer = android.media.MediaPlayer.create(this, R.raw.notification_sound)
            mediaPlayer?.setOnCompletionListener { it.release() }
            mediaPlayer?.start()
            Log.d(TAG, "Playing notification sound")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play notification sound", e)
        }
    }
    
    private fun showNotification(title: String, body: String, orderId: String? = null) {
        createNotificationChannel()
        
        val intent = if (orderId != null) {
            Intent(this, OrderDetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("orderId", orderId)
            }
        } else {
            Intent(this, DriverHomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            orderId?.toIntOrNull() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val soundUri = Uri.parse("android.resource://${packageName}/${R.raw.notification_sound}")
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("android.resource://${packageName}/${R.raw.notification_sound}")
            
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new delivery orders"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        
        val packageName = applicationContext.packageName
        return appProcesses.any { process ->
            process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    process.processName == packageName
        }
    }
}
