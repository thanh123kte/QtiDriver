package com.qtifood.driver.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.qtifood.driver.databinding.ActivityLoginBinding
import com.qtifood.driver.presentation.driverinfo.DriverInfoActivity
import com.qtifood.driver.presentation.home.DriverHomeActivity
import com.qtifood.driver.presentation.main.MainActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModel()
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.etPhoneNumber.addTextChangedListener {
            viewModel.updatePhoneNumber(it.toString())
        }

        binding.etCountryCode.addTextChangedListener {
            viewModel.updateCountryCode(it.toString())
        }

        binding.btnLogin.setOnClickListener {
            val uiState = viewModel.uiState.value
            if (uiState.isVerificationCodeSent) {
                val code = binding.etVerificationCode.text.toString()
                if (code.isNotEmpty()) {
                    verifyCode(code)
                } else {
                    Toast.makeText(this, "Vui lòng nhập mã xác thực", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (viewModel.isPhoneNumberValid()) {
                    sendVerificationCode()
                } else {
                    Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnLogin.isEnabled = !state.isLoading

                state.errorMessage?.let { message ->
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                if (state.isVerificationCodeSent && binding.tilVerificationCode.visibility == View.GONE) {
                    binding.tilVerificationCode.visibility = View.VISIBLE
                    binding.btnLogin.text = "Xác thực"
                    Toast.makeText(this@LoginActivity, "Mã xác thực đã được gửi", Toast.LENGTH_SHORT).show()
                }

                if (state.shouldNavigateToDriverInfo) {
                    navigateToDriverInfo(state.firebaseUid ?: "")
                    viewModel.resetNavigation()
                }

                if (state.shouldNavigateToHome) {
                    navigateToHome()
                    viewModel.resetNavigation()
                }
            }
        }
    }

    private fun sendVerificationCode() {
        val phoneNumber = viewModel.getFullPhoneNumber()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    viewModel.signInWithPhoneCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Xác thực thất bại: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    viewModel.setVerificationId(verificationId)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        storedVerificationId?.let { verificationId ->
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            viewModel.signInWithPhoneCredential(credential)
        }
    }

    private fun navigateToDriverInfo(firebaseUid: String) {
        val intent = Intent(this, DriverInfoActivity::class.java).apply {
            putExtra("FIREBASE_UID", firebaseUid)
            putExtra("PHONE_NUMBER", viewModel.getFullPhoneNumber())
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, DriverHomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
