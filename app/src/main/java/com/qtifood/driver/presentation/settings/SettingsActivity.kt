package com.qtifood.driver.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.qtifood.driver.databinding.ActivitySettingsBinding
import com.qtifood.driver.presentation.documents.DocumentsActivity
import com.qtifood.driver.presentation.login.LoginActivity
import com.qtifood.driver.presentation.profile.DriverProfileActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // Personal Information
        binding.cardPersonalInfo.setOnClickListener {
            val intent = Intent(this, DriverProfileActivity::class.java)
            startActivity(intent)
        }

        // Documents
        binding.cardDocuments.setOnClickListener {
            val intent = Intent(this, DocumentsActivity::class.java)
            startActivity(intent)
        }

        // Change Password (placeholder)
        binding.cardChangePassword.setOnClickListener {
            // Show FCM token for debugging
            getFCMToken()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("SettingsActivity", "Failed to get FCM token", task.exception)
                Toast.makeText(this, "Lỗi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d("SettingsActivity", "FCM Token: $token")
            
            // Copy to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("FCM Token", token)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(
                this,
                "Token copied!\n${token.take(30)}...",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
