package com.qtifood.driver.presentation.driverinfo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.qtifood.driver.databinding.ActivityDriverInfoBinding
import com.qtifood.driver.presentation.home.DriverHomeActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DriverInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverInfoBinding
    private val viewModel: DriverInfoViewModel by viewModel()
    private var firebaseUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUid = intent.getStringExtra("FIREBASE_UID") ?: ""
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""

        setupUI(phoneNumber)
        observeViewModel()
    }

    private fun setupUI(phoneNumber: String) {
        binding.etPhone.setText(phoneNumber)
        viewModel.updatePhoneNumber(phoneNumber)

        binding.etFullName.addTextChangedListener {
            viewModel.updateFullName(it.toString())
        }

        binding.etCccd.addTextChangedListener {
            viewModel.updateCccdNumber(it.toString())
        }

        binding.etLicense.addTextChangedListener {
            viewModel.updateLicenseNumber(it.toString())
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.submitDriverInfo(firebaseUid)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnSubmit.isEnabled = !state.isLoading && state.isFormValid

                state.errorMessage?.let { message ->
                    Toast.makeText(this@DriverInfoActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                if (state.isSubmitSuccess) {
                    Toast.makeText(
                        this@DriverInfoActivity,
                        "Đăng ký thành công! Vui lòng chờ xác thực.",
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToMain()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, DriverHomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
