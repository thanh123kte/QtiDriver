package com.qtifood.driver.presentation.profile

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.qtifood.driver.R
import com.qtifood.driver.core.Constants
import com.qtifood.driver.databinding.ActivityDriverProfileBinding
import com.qtifood.driver.domain.model.VerificationStatus
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.Calendar

class DriverProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverProfileBinding
    private val viewModel: DriverProfileViewModel by viewModel()
    
    private var selectedImageUri: Uri? = null
    private var currentPhotoUri: Uri = Uri.EMPTY
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivAvatar.setImageURI(it)
            viewModel.setAvatarUri(it.toString())
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentPhotoUri?.let {
                selectedImageUri = it
                binding.ivAvatar.setImageURI(it)
                viewModel.setAvatarUri(it.toString())
            }
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Cần quyền truy cập camera", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        
        // Load driver profile
        val driverId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (driverId.isNotEmpty()) {
            viewModel.loadDriverProfile(driverId)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // Change avatar
        binding.fabChangeAvatar.setOnClickListener {
            showImageSourceDialog()
        }
        
        // Date picker
        binding.tilDateOfBirth.setEndIconOnClickListener {
            showDatePicker()
        }
        
        binding.etDateOfBirth.setOnClickListener {
            showDatePicker()
        }
        
        // Save button
        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }
    
    private fun showImageSourceDialog() {
        val options = arrayOf("Chụp ảnh", "Chọn từ thư viện")
        AlertDialog.Builder(this)
            .setTitle("Chọn nguồn ảnh")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> imagePickerLauncher.launch("image/*")
                }
            }
            .show()
    }
    
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun openCamera() {
        val photoFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}",
            ".jpg",
            cacheDir
        )
        
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        
        cameraLauncher.launch(currentPhotoUri)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnSave.isEnabled = !state.isLoading
                
                state.driver?.let { driver ->
                    // Update UI with driver data
                    binding.etFullName.setText(driver.fullName)
                    binding.etPhone.setText(driver.phone)
                    
                    // Convert date from yyyy-MM-dd to dd/MM/yyyy for display
                    val displayDate = if (!driver.dateOfBirth.isNullOrEmpty()) {
                        convertDateForDisplay(driver.dateOfBirth)
                    } else {
                        ""
                    }
                    binding.etDateOfBirth.setText(displayDate)
                    
                    binding.etEmail.setText(driver.email ?: "")
                    binding.etAddress.setText(driver.address ?: "")
                    binding.etCccd.setText(driver.cccdNumber)
                    binding.etLicense.setText(driver.licenseNumber)
                    
                    // Update approval status
                    updateApprovalStatus(driver.verificationStatus)
                    
                    // Load avatar
                    if (!driver.avatarUrl.isNullOrEmpty()) {
                        Glide.with(this@DriverProfileActivity)
                            .load(Constants.getImageUrl(driver.avatarUrl))
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(binding.ivAvatar)
                    }
                }
                
                state.errorMessage?.let { message ->
                    Toast.makeText(this@DriverProfileActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
                
                if (state.isUpdateSuccess) {
                    Toast.makeText(
                        this@DriverProfileActivity,
                        "Cập nhật thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.clearSuccess()
                    finish()
                }
                
                binding.tvApprovalStatus.text = state.approvalStatusText
            }
        }
    }

    private fun updateApprovalStatus(status: VerificationStatus) {
        when (status) {
            VerificationStatus.PENDING -> {
                binding.tvApprovalStatus.text = "Đang chờ duyệt"
                binding.tvApprovalStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.orange_primary)
                )
            }
            VerificationStatus.APPROVED -> {
                binding.tvApprovalStatus.text = "Đã được duyệt"
                binding.tvApprovalStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.status_online)
                )
            }
            VerificationStatus.REJECTED -> {
                binding.tvApprovalStatus.text = "Bị từ chối"
                binding.tvApprovalStatus.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
                )
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // Parse current date if exists
        val currentDate = binding.etDateOfBirth.text.toString()
        if (currentDate.isNotEmpty()) {
            try {
                val parts = currentDate.split("/")
                if (parts.size == 3) {
                    calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                }
            } catch (e: Exception) {
                // Use current date
            }
        }
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val dateStr = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                binding.etDateOfBirth.setText(dateStr)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set max date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        datePickerDialog.show()
    }
    
    private fun convertDateForDisplay(dateStr: String): String {
        return try {
            // Input format: yyyy-MM-dd
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1]
                val day = parts[2]
                // Output format: dd/MM/yyyy
                "$day/$month/$year"
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val dateOfBirthInput = binding.etDateOfBirth.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val cccdNumber = binding.etCccd.text.toString().trim()
        val licenseNumber = binding.etLicense.text.toString().trim()
        
        // Validation
        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Vui lòng nhập họ tên"
            return
        }
        
        if (cccdNumber.isEmpty()) {
            binding.tilCccd.error = "Vui lòng nhập số CCCD"
            return
        }
        
        if (licenseNumber.isEmpty()) {
            binding.tilLicense.error = "Vui lòng nhập số giấy phép"
            return
        }
        
        // Clear errors
        binding.tilFullName.error = null
        binding.tilCccd.error = null
        binding.tilLicense.error = null
        
        // Convert date format from dd/MM/yyyy to yyyy-MM-dd
        val dateOfBirth = if (dateOfBirthInput.isNotEmpty()) {
            convertDateFormat(dateOfBirthInput)
        } else {
            ""
        }
        
        // Update profile
        viewModel.updateDriverProfile(
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            email = email,
            address = address,
            cccdNumber = cccdNumber,
            licenseNumber = licenseNumber,
            avatarUri = selectedImageUri?.toString()
        )
    }
    
    private fun convertDateFormat(dateStr: String): String {
        return try {
            // Input format: dd/MM/yyyy
            val parts = dateStr.split("/")
            if (parts.size == 3) {
                val day = parts[0]
                val month = parts[1]
                val year = parts[2]
                // Output format: yyyy-MM-dd
                "$year-$month-$day"
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}
