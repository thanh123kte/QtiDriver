package com.qtifood.driver.presentation.common

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.qtifood.driver.R
import com.qtifood.driver.presentation.order.OrderDetailActivity
import com.qtifood.driver.presentation.wallet.WalletActivity

object NotificationPopup {
    
    private const val TAG = "NotificationPopup"
    private var currentView: View? = null
    private var windowManager: WindowManager? = null
    
    fun showOrderNotification(
        context: Context,
        title: String,
        message: String,
        orderId: String
    ) {
        Log.d(TAG, "showOrderNotification: $title")
        dismissCurrent()
        
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager = wm
            
            val view = LayoutInflater.from(context).inflate(R.layout.popup_notification_order, null)
            
            // Set content
            view.findViewById<ImageView>(R.id.ivIcon).setImageResource(R.drawable.ic_delivery_order)
            view.findViewById<TextView>(R.id.tvTitle).text = title
            view.findViewById<TextView>(R.id.tvMessage).text = message
            
            // View details button
            view.findViewById<Button>(R.id.btnViewDetails).setOnClickListener {
                val intent = Intent(context, OrderDetailActivity::class.java).apply {
                    putExtra("orderId", orderId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                dismissCurrent()
            }
            
            // Dismiss button
            view.findViewById<Button>(R.id.btnDismiss).setOnClickListener {
                dismissCurrent()
            }
            
            // Add to window
            val params = createWindowParams(context)
            wm.addView(view, params)
            currentView = view
            
            // Auto dismiss after 10 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                dismissCurrent()
            }, 10000)
            
            Log.d(TAG, "Order notification shown successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show order notification", e)
        }
    }
    
    fun showWalletNotification(
        context: Context,
        title: String,
        message: String,
        notificationType: String
    ) {
        Log.d(TAG, "showWalletNotification: $title")
        dismissCurrent()
        
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager = wm
            
            val view = LayoutInflater.from(context).inflate(R.layout.popup_notification_wallet, null)
            
            // Set icon based on type
            val iconRes = when (notificationType) {
                "TOPUP" -> R.drawable.ic_wallet
                "WITHDRAWAL" -> R.drawable.ic_withdrawal
                else -> R.drawable.ic_wallet
            }
            
            view.findViewById<ImageView>(R.id.ivIcon).setImageResource(iconRes)
            view.findViewById<TextView>(R.id.tvTitle).text = title
            view.findViewById<TextView>(R.id.tvMessage).text = message
            
            // View wallet button
            view.findViewById<Button>(R.id.btnViewWallet).setOnClickListener {
                val intent = Intent(context, WalletActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                dismissCurrent()
            }
            
            // Dismiss button
            view.findViewById<Button>(R.id.btnDismiss).setOnClickListener {
                dismissCurrent()
            }
            
            // Add to window
            val params = createWindowParams(context)
            wm.addView(view, params)
            currentView = view
            
            // Auto dismiss after 8 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                dismissCurrent()
            }, 8000)
            
            Log.d(TAG, "Wallet notification shown successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show wallet notification", e)
        }
    }
    
    fun showGenericNotification(
        context: Context,
        title: String,
        message: String
    ) {
        Log.d(TAG, "showGenericNotification: $title")
        dismissCurrent()
        
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager = wm
            
            val view = LayoutInflater.from(context).inflate(R.layout.popup_notification_generic, null)
            
            view.findViewById<ImageView>(R.id.ivIcon).setImageResource(R.drawable.ic_notification)
            view.findViewById<TextView>(R.id.tvTitle).text = title
            view.findViewById<TextView>(R.id.tvMessage).text = message
            
            // Dismiss button
            view.findViewById<Button>(R.id.btnDismiss).setOnClickListener {
                dismissCurrent()
            }
            
            // Add to window
            val params = createWindowParams(context)
            wm.addView(view, params)
            currentView = view
            
            // Auto dismiss after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                dismissCurrent()
            }, 5000)
            
            Log.d(TAG, "Generic notification shown successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show generic notification", e)
        }
    }
    
    private fun createWindowParams(context: Context): WindowManager.LayoutParams {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 100 // Offset from top
        }
    }
    
    private fun dismissCurrent() {
        try {
            currentView?.let { view ->
                windowManager?.removeView(view)
                Log.d(TAG, "Notification dismissed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing notification", e)
        } finally {
            currentView = null
            windowManager = null
        }
    }
}
