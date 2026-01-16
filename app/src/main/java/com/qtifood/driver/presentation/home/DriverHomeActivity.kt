package com.qtifood.driver.presentation.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.runBlocking
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.qtifood.driver.R
import com.qtifood.driver.core.Constants
import com.qtifood.driver.databinding.ActivityDriverHomeBinding
import com.qtifood.driver.domain.model.VerificationStatus
import com.qtifood.driver.presentation.login.LoginActivity
import com.qtifood.driver.presentation.order.OrderDetailActivity
import com.qtifood.driver.presentation.profile.DriverProfileActivity
import com.qtifood.driver.presentation.settings.SettingsActivity
import com.qtifood.driver.service.LocationTrackingService
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class DriverHomeActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityDriverHomeBinding
    private val viewModel: DriverHomeViewModel by viewModel()
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var currentLocationMarker: Marker? = null
    
    // Flag to prevent infinite loop when updating switch programmatically
    private var isUpdatingSwitch = false

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                enableMyLocation()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Cần cấp quyền truy cập vị trí để sử dụng tính năng này",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    // Notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled silently
    }
    
    // Overlay permission launcher
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Quyền hiển thị thông báo đã được cấp", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
        
        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupNavigationDrawer()
        setupMap()
        setupClickListeners()
        observeViewModel()
        checkLocationPermission()
        checkNotificationPermission()
        checkOverlayPermission()
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupMap() {
        // Configure map
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(15.0)
        
        // Set default location (Hanoi, Vietnam)
        val defaultLocation = GeoPoint(21.028511, 105.804817)
        binding.mapView.controller.setCenter(defaultLocation)
        
        // Setup location overlay
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.mapView)
        myLocationOverlay?.enableMyLocation()
        binding.mapView.overlays.add(myLocationOverlay)
    }

    private fun setupClickListeners() {
        // Menu button
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Online/Offline toggle
        binding.switchOnlineStatus.setOnCheckedChangeListener { _, isChecked ->
            // Ignore if we're updating the switch programmatically
            if (isUpdatingSwitch) return@setOnCheckedChangeListener
            
            if (isChecked) {
                // Check verification status before allowing online
                val driver = viewModel.uiState.value.driver
                if (driver == null) {
                    Toast.makeText(this, "Đang tải thông tin tài xế...", Toast.LENGTH_SHORT).show()
                    isUpdatingSwitch = true
                    binding.switchOnlineStatus.isChecked = false
                    isUpdatingSwitch = false
                    return@setOnCheckedChangeListener
                }
                
                when (driver.verificationStatus) {
                    VerificationStatus.PENDING -> {
                        // Show dialog for pending verification
                        showPendingVerificationDialog()
                        isUpdatingSwitch = true
                        binding.switchOnlineStatus.isChecked = false
                        isUpdatingSwitch = false
                    }
                    VerificationStatus.REJECTED -> {
                        Toast.makeText(
                            this,
                            "Hồ sơ của bạn đã bị từ chối. Vui lòng cập nhật lại thông tin.",
                            Toast.LENGTH_LONG
                        ).show()
                        isUpdatingSwitch = true
                        binding.switchOnlineStatus.isChecked = false
                        isUpdatingSwitch = false
                    }
                    VerificationStatus.APPROVED -> {
                        // Allow to go online
                        viewModel.toggleOnlineStatus()
                    }
                }
            } else {
                viewModel.toggleOnlineStatus()
            }
        }

        // Current location button
        binding.fabCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val wasOnline = binding.switchOnlineStatus.isChecked
                val isNowOnline = state.isOnline
                
                updateOnlineStatus(isNowOnline)
                
                // Start/Stop location tracking service when online status changes
                if (wasOnline != isNowOnline) {
                    handleLocationTrackingService(isNowOnline, state.driver?.id)
                }
                
                // Update driver info in nav header
                state.driver?.let { driver ->
                    updateNavHeader(driver)
                }
                
                state.currentLocation?.let { location ->
                    updateDriverLocationOnMap(location.latitude, location.longitude)
                }
                
                state.errorMessage?.let { message ->
                    Toast.makeText(this@DriverHomeActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }
    
    private fun handleLocationTrackingService(isOnline: Boolean, driverId: String?) {
        if (isOnline && driverId != null) {
            Log.d("DriverHomeActivity", "Starting location tracking service for driver: $driverId")
            LocationTrackingService.start(this, driverId)
        } else {
            Log.d("DriverHomeActivity", "Stopping location tracking service")
            LocationTrackingService.stop(this)
        }
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        isUpdatingSwitch = true
        if (isOnline) {
            binding.tvStatusValue.text = "Đang online"
            binding.tvStatusValue.setTextColor(ContextCompat.getColor(this, R.color.status_online))
            binding.switchOnlineStatus.isChecked = true
            binding.switchOnlineStatus.text = "Online"
        } else {
            binding.tvStatusValue.text = "Offline"
            binding.tvStatusValue.setTextColor(ContextCompat.getColor(this, R.color.status_offline))
            binding.switchOnlineStatus.isChecked = false
            binding.switchOnlineStatus.text = "Offline"
        }
        binding.switchOnlineStatus.isEnabled = viewModel.uiState.value.isToggleEnabled
        isUpdatingSwitch = false
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun enableMyLocation() {
        myLocationOverlay?.enableMyLocation()
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                binding.mapView.controller.animateTo(geoPoint)
                binding.mapView.controller.setZoom(17.0)
                viewModel.updateLocation(it.latitude, it.longitude)
            }
        }
    }

    private fun updateDriverLocationOnMap(latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        
        // Remove old marker if exists
        currentLocationMarker?.let {
            binding.mapView.overlays.remove(it)
        }
        
        // Add new marker
        currentLocationMarker = Marker(binding.mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Vị trí của bạn"
        }
        binding.mapView.overlays.add(currentLocationMarker)
        binding.mapView.invalidate()
    }
    
    private fun updateNavHeader(driver: com.qtifood.driver.domain.model.Driver) {
        val headerView = binding.navigationView.getHeaderView(0)
        val ivAvatar = headerView.findViewById<ShapeableImageView>(R.id.ivDriverAvatar)
        val tvName = headerView.findViewById<TextView>(R.id.tvDriverName)
        val tvPhone = headerView.findViewById<TextView>(R.id.tvDriverPhone)
        val tvStatus = headerView.findViewById<TextView>(R.id.tvVerificationStatus)
        val tvAccountStatus = headerView.findViewById<TextView>(R.id.tvAccountStatus)
        
        // Set name
        tvName.text = driver.fullName
        
        // Set phone
        tvPhone.text = driver.phone
        
        // Set avatar
        if (!driver.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(Constants.getImageUrl(driver.avatarUrl))
                .placeholder(R.drawable.ic_default_avatar)
                .into(ivAvatar)
        }
        
        // Set verification status
        when (driver.verificationStatus) {
            VerificationStatus.PENDING -> {
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "Đang chờ duyệt"
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
            VerificationStatus.APPROVED -> {
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "Đã được duyệt"
                tvStatus.setBackgroundResource(R.drawable.bg_status_approved)
            }
            VerificationStatus.REJECTED -> {
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "Bị từ chối"
                tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
            }
        }

        // Set account (online) status
        val statusColor = when (driver.status?.uppercase()) {
            "ONLINE" -> ContextCompat.getColor(this, R.color.status_online)
            "BUSY" -> ContextCompat.getColor(this, R.color.status_pending)
            else -> ContextCompat.getColor(this, R.color.status_offline)
        }
        val statusLabel = when (driver.status?.uppercase()) {
            "ONLINE" -> "Đang online"
            "BUSY" -> "Đang bận"
            else -> "Offline"
        }
        tvAccountStatus.visibility = View.VISIBLE
        tvAccountStatus.text = statusLabel
        tvAccountStatus.backgroundTintList = ColorStateList.valueOf(statusColor)
    }
    
    private fun showPendingVerificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Chưa thể hoạt động")
            .setMessage("Vui lòng đợi hồ sơ được duyệt, bạn sẽ có thể hoạt động sớm thôi!")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Show dialog explaining why we need this permission
                AlertDialog.Builder(this)
                    .setTitle("Cần quyền hiển thị thông báo")
                    .setMessage("Ứng dụng cần quyền hiển thị thông báo popup khi có đơn hàng mới. Vui lòng cấp quyền trong cài đặt.")
                    .setPositiveButton("Cài đặt") { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        overlayPermissionLauncher.launch(intent)
                    }
                    .setNegativeButton("Để sau", null)
                    .show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                viewModel.refreshDriverData()
            }
            R.id.nav_wallet -> {
                val intent = Intent(this, com.qtifood.driver.presentation.wallet.WalletActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_income -> {
                val intent = Intent(this, com.qtifood.driver.presentation.income.IncomeStatsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_orders -> {
                openCurrentOrder()
            }
            R.id.nav_history -> {
                val intent = Intent(this, com.qtifood.driver.presentation.history.DeliveryHistoryActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        
        // Refresh driver data when returning to home screen
        viewModel.refreshDriverData()
        
        // Reset menu selection to Home when returning from other screens
        binding.navigationView.setCheckedItem(R.id.nav_home)
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        if (viewModel.uiState.value.isOnline) {
            AlertDialog.Builder(this)
                .setTitle("Tắt trạng thái hoạt động")
                .setMessage("Vui lòng tắt Online trước khi thoát ứng dụng.")
                .setPositiveButton("Đóng ứng dụng") { _, _ ->
                    lifecycleScope.launch {
                        viewModel.forceSetOffline()
                        finish()
                    }
                }
                .setNegativeButton("Ở lại", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun openCurrentOrder() {
        val driver = viewModel.uiState.value.driver
        if (driver == null) {
            Toast.makeText(this, "Dang tai thong tin tai xe", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val tracking = viewModel.getCurrentOrderForDriver()
            if (tracking == null || tracking.orderId == 0L) {
                Toast.makeText(this@DriverHomeActivity, "Chua co don hien tai", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val intent = Intent(this@DriverHomeActivity, OrderDetailActivity::class.java).apply {
                putExtra("orderId", tracking.orderId.toString())
                tracking.customerName?.let { putExtra("customerName", it) }
                tracking.customerPhone?.let { putExtra("customerPhone", it) }
                tracking.storeAddress?.let { putExtra("pickupAddress", it) }
                tracking.shippingAddress?.let { putExtra("deliveryAddress", it) }
                tracking.status.let { putExtra("status", it ?: "") }
                // Keep createdAt fallback to current time if cannot parse
            }
            startActivity(intent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Stop location tracking service when activity is destroyed
        LocationTrackingService.stop(this)
    }

}
