package com.qtifood.driver.presentation.documents

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
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
import com.qtifood.driver.databinding.ActivityDocumentsBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class DocumentsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DocumentsActivity"
    }

    private lateinit var binding: ActivityDocumentsBinding
    private val viewModel: DriverDocumentsViewModel by viewModel()
    
    private var cccdFrontUri: Uri? = null
    private var cccdBackUri: Uri? = null
    private var licenseUri: Uri? = null
    private var vehicleRegistrationUri: Uri? = null
    private var vehiclePlateUri: Uri? = null
    
    private var currentImageType: ImageType = ImageType.CCCD_FRONT
    private var currentPhotoUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleImageSelected(it)
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentPhotoUri?.let {
                handleImageSelected(it)
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
        binding = ActivityDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        
        // Load driver documents
        val driverId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (driverId.isNotEmpty()) {
            viewModel.loadDocuments(driverId)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.fabCccdFront.setOnClickListener {
            currentImageType = ImageType.CCCD_FRONT
            showImageSourceDialog()
        }
        
        binding.fabCccdBack.setOnClickListener {
            currentImageType = ImageType.CCCD_BACK
            showImageSourceDialog()
        }
        
        binding.fabLicense.setOnClickListener {
            currentImageType = ImageType.LICENSE
            showImageSourceDialog()
        }
        
        binding.fabVehicleRegistration.setOnClickListener {
            currentImageType = ImageType.VEHICLE_REGISTRATION
            showImageSourceDialog()
        }
        
        binding.fabVehiclePlate.setOnClickListener {
            currentImageType = ImageType.VEHICLE_PLATE
            showImageSourceDialog()
        }
        
        binding.btnSave.setOnClickListener {
            saveDocuments()
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
        
        cameraLauncher.launch(currentPhotoUri!!)
    }
    
    private fun handleImageSelected(uri: Uri) {
        Log.d(TAG, "Image selected for type: $currentImageType")
        Log.d(TAG, "URI: $uri")
        
        when (currentImageType) {
            ImageType.CCCD_FRONT -> {
                cccdFrontUri = uri
                binding.ivCccdFront.setImageURI(uri)
                binding.ivCccdFront.scaleType = ImageView.ScaleType.CENTER_CROP
                Log.d(TAG, "CCCD Front URI saved: $cccdFrontUri")
            }
            ImageType.CCCD_BACK -> {
                cccdBackUri = uri
                binding.ivCccdBack.setImageURI(uri)
                binding.ivCccdBack.scaleType = ImageView.ScaleType.CENTER_CROP
                Log.d(TAG, "CCCD Back URI saved: $cccdBackUri")
            }
            ImageType.LICENSE -> {
                licenseUri = uri
                binding.ivLicense.setImageURI(uri)
                binding.ivLicense.scaleType = ImageView.ScaleType.CENTER_CROP
                Log.d(TAG, "License URI saved: $licenseUri")
            }
            ImageType.VEHICLE_REGISTRATION -> {
                vehicleRegistrationUri = uri
                binding.ivVehicleRegistration.setImageURI(uri)
                binding.ivVehicleRegistration.scaleType = ImageView.ScaleType.CENTER_CROP
                Log.d(TAG, "Vehicle Registration URI saved: $vehicleRegistrationUri")
            }
            ImageType.VEHICLE_PLATE -> {
                vehiclePlateUri = uri
                binding.ivVehiclePlate.setImageURI(uri)
                binding.ivVehiclePlate.scaleType = ImageView.ScaleType.CENTER_CROP
                Log.d(TAG, "Vehicle Plate URI saved: $vehiclePlateUri")
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnSave.isEnabled = !state.isLoading
                
                state.driver?.let { driver ->
                    // Load existing images
                    if (!driver.cccdFrontImageUrl.isNullOrEmpty()) {
                        Glide.with(this@DocumentsActivity)
                            .load(Constants.getImageUrl(driver.cccdFrontImageUrl))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(binding.ivCccdFront)
                    }
                    
                    if (!driver.cccdBackImageUrl.isNullOrEmpty()) {
                        Glide.with(this@DocumentsActivity)
                            .load(Constants.getImageUrl(driver.cccdBackImageUrl))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(binding.ivCccdBack)
                    }
                    
                    if (!driver.licenseImageUrl.isNullOrEmpty()) {
                        Glide.with(this@DocumentsActivity)
                            .load(Constants.getImageUrl(driver.licenseImageUrl))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(binding.ivLicense)
                    }
                    
                    if (!driver.vehicleRegistrationImageUrl.isNullOrEmpty()) {
                        Glide.with(this@DocumentsActivity)
                            .load(Constants.getImageUrl(driver.vehicleRegistrationImageUrl))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(binding.ivVehicleRegistration)
                    }
                    
                    if (!driver.vehiclePlateImageUrl.isNullOrEmpty()) {
                        Glide.with(this@DocumentsActivity)
                            .load(Constants.getImageUrl(driver.vehiclePlateImageUrl))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(binding.ivVehiclePlate)
                    }
                }
                
                state.errorMessage?.let { message ->
                    Toast.makeText(this@DocumentsActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
                
                if (state.isUpdateSuccess) {
                    Toast.makeText(
                        this@DocumentsActivity,
                        "Cập nhật giấy tờ thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.clearSuccess()
                    finish()
                }
            }
        }
    }

    private fun saveDocuments() {
        Log.d(TAG, "=== Save button clicked ===")
        Log.d(TAG, "CCCD Front URI: $cccdFrontUri")
        Log.d(TAG, "CCCD Back URI: $cccdBackUri")
        Log.d(TAG, "License URI: $licenseUri")
        Log.d(TAG, "Vehicle Registration URI: $vehicleRegistrationUri")
        Log.d(TAG, "Vehicle Plate URI: $vehiclePlateUri")
        
        viewModel.updateDocuments(
            cccdFrontImageUrl = cccdFrontUri?.toString(),
            cccdBackImageUrl = cccdBackUri?.toString(),
            licenseImageUrl = licenseUri?.toString(),
            vehicleRegistrationImageUrl = vehicleRegistrationUri?.toString(),
            vehiclePlateImageUrl = vehiclePlateUri?.toString()
        )
    }
    
    private enum class ImageType {
        CCCD_FRONT,
        CCCD_BACK,
        LICENSE,
        VEHICLE_REGISTRATION,
        VEHICLE_PLATE
    }
}
