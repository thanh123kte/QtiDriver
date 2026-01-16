package com.qtifood.driver.presentation.order

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qtifood.driver.R
import com.qtifood.driver.core.Constants
import com.qtifood.driver.data.firebase.OrderTrackingDataSource
import com.qtifood.driver.data.remote.dto.OrderDetailDto
import com.qtifood.driver.data.remote.dto.OrderItemDto
import com.qtifood.driver.databinding.ActivityOrderDetailBinding
import com.qtifood.driver.domain.model.Order
import com.qtifood.driver.domain.model.OrderStatus
import com.qtifood.driver.domain.repository.OrderRepository
import com.qtifood.driver.presentation.home.DriverHomeActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

class OrderDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOrderDetailBinding
    private val orderRepository: OrderRepository by inject()
    private val orderItemsAdapter = OrderItemsAdapter()
    private val orderTrackingDataSource: OrderTrackingDataSource by inject()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var locationCallback: LocationCallback? = null
    private var trackingRef: DatabaseReference? = null
    private var trackingListener: ValueEventListener? = null
    private var activeOrderId: Int? = null
    
    private var order: Order? = null
    private var orderItems: List<OrderItemDto> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadOrderFromIntent()
    }

    companion object {
        private const val TAG = "OrderDetailActivity"
        
        @JvmStatic
        fun createIntent(
            context: android.content.Context,
            orderId: String,
            customerName: String? = null,
            customerPhone: String? = null,
            pickupAddress: String? = null,
            deliveryAddress: String? = null,
            status: String? = null,
            createdAt: Long? = null
        ): android.content.Intent {
            return android.content.Intent(context, OrderDetailActivity::class.java).apply {
                putExtra("orderId", orderId)
                customerName?.let { putExtra("customerName", it) }
                customerPhone?.let { putExtra("customerPhone", it) }
                pickupAddress?.let { putExtra("pickupAddress", it) }
                deliveryAddress?.let { putExtra("deliveryAddress", it) }
                status?.let { putExtra("status", it) }
                createdAt?.let { putExtra("createdAt", it) }
            }
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Chi tiết đơn hàng"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun setupRecyclerView() {
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailActivity)
            adapter = orderItemsAdapter
        }
    }
    
    private fun loadOrderFromIntent() {
        val orderIdFromString = intent.getStringExtra("orderId")
        val orderIdFromInt = intent.getIntExtra("orderId", -1).takeIf { it > 0 }?.toString()
        val orderIdValue = orderIdFromString ?: orderIdFromInt
        
        if (orderIdValue.isNullOrBlank()) {
            Toast.makeText(this, "Khong tim thay orderId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val orderId = orderIdValue.toIntOrNull()
        if (orderId == null) {
            Toast.makeText(this, "orderId khong hop le", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        activeOrderId = orderId
        startTrackingRealtime(orderId)
        startLocationUpdates(orderId)
        prefillOrderIfProvided(orderIdValue)
        fetchOrderDetail(orderId)
    }
    
    private fun prefillOrderIfProvided(orderId: String) {
        val customerName = intent.getStringExtra("customerName")
        if (customerName != null) {
            order = Order(
                id = orderId,
                customerName = customerName,
                customerPhone = intent.getStringExtra("customerPhone") ?: "",
                pickupAddress = intent.getStringExtra("pickupAddress") ?: "",
                deliveryAddress = intent.getStringExtra("deliveryAddress") ?: "",
                totalAmount = intent.getDoubleExtra("totalAmount", 0.0),
                status = mapApiStatusToOrderStatus(intent.getStringExtra("status") ?: "PENDING"),
                createdAt = intent.getLongExtra("createdAt", System.currentTimeMillis())
            )
            displayOrderDetails()
        }
    }
    
    private fun fetchOrderDetail(orderId: Int) {
        lifecycleScope.launch {
            setLoading(true)
            
            val trackingSnapshot = runCatching { fetchTrackingSnapshot(orderId) }
                .onFailure { Log.w(TAG, "Failed to load tracking from Firebase: ${it.message}") }
                .getOrNull()
            
            val orderDetail = orderRepository.getOrderById(orderId).getOrElse { error ->
                Log.e(TAG, "API getOrderById failed", error)
                setLoading(false)
                Toast.makeText(
                    this@OrderDetailActivity,
                    "Khong tai duoc don hang: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }
            
            val addressId = trackingSnapshot?.shippingAddressId
                ?: orderDetail.shippingAddressId.takeIf { it > 0 }
            val addressDto = addressId?.let { id ->
                orderRepository.getAddressById(id).getOrNull()
            }
            
            orderItems = orderRepository.getOrderItems(orderId).getOrElse { error ->
                Log.w(TAG, "API getOrderItems failed", error)
                Toast.makeText(
                    this@OrderDetailActivity,
                    "Khong tai duoc danh sach san pham",
                    Toast.LENGTH_SHORT
                ).show()
                emptyList()
            }
            
            order = mapToOrder(orderDetail, trackingSnapshot, addressDto)
            displayOrderDetails()
            renderOrderItems()
            setLoading(false)
        }
    }
    
    private suspend fun fetchTrackingSnapshot(orderId: Int): TrackingSnapshot? =
        suspendCancellableCoroutine { continuation ->
            val ref = FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL)
                .getReference("order_tracking")
                .child(orderId.toString())
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        continuation.resume(null)
                        return
                    }
                    
                    val tracking = TrackingSnapshot(
                        shippingAddress = snapshot.child("shippingAddress").getValue(String::class.java).orEmpty(),
                        storeAddress = snapshot.child("storeAddress").getValue(String::class.java).orEmpty(),
                        customerName = snapshot.child("customerName").getValue(String::class.java).orEmpty(),
                        customerPhone = snapshot.child("customerPhone").getValue(String::class.java).orEmpty(),
                        status = snapshot.child("status").getValue(String::class.java).orEmpty(),
                        assignedAt = snapshot.child("assignedAt").getValue(String::class.java).orEmpty(),
                        shippingAddressId = snapshot.child("shippingAddressId").value?.toString()?.toIntOrNull()
                    )
                    continuation.resume(tracking)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase tracking cancelled: ${error.message}")
                    continuation.resume(null)
                }
            }
            
            ref.addListenerForSingleValueEvent(listener)
            continuation.invokeOnCancellation { ref.removeEventListener(listener) }
        }
    
    private fun startTrackingRealtime(orderId: Int) {
        val ref = FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL)
            .getReference("order_tracking")
            .child(orderId.toString())
        trackingRef = ref
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val tracking = TrackingSnapshot(
                    shippingAddress = snapshot.child("shippingAddress").getValue(String::class.java).orEmpty(),
                    storeAddress = snapshot.child("storeAddress").getValue(String::class.java).orEmpty(),
                    customerName = snapshot.child("customerName").getValue(String::class.java).orEmpty(),
                    customerPhone = snapshot.child("customerPhone").getValue(String::class.java).orEmpty(),
                    status = snapshot.child("status").getValue(String::class.java).orEmpty(),
                    assignedAt = snapshot.child("assignedAt").getValue(String::class.java).orEmpty(),
                    shippingAddressId = snapshot.child("shippingAddressId").value?.toString()?.toIntOrNull()
                )
                applyTrackingUpdate(tracking)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Tracking listener cancelled: ${error.message}")
            }
        }
        trackingListener = listener
        ref.addValueEventListener(listener)
    }
    
    private fun applyTrackingUpdate(tracking: TrackingSnapshot) {
        val existing = order
        order = if (existing != null) {
            existing.copy(
                pickupAddress = tracking.storeAddress.ifBlank { existing.pickupAddress },
                deliveryAddress = tracking.shippingAddress.ifBlank { existing.deliveryAddress },
                status = mapApiStatusToOrderStatus(tracking.status.ifBlank { existing.status.name }),
                createdAt = tracking.assignedAt?.let { parseFlexibleDate(it) } ?: existing.createdAt
            )
        } else {
            Order(
                id = activeOrderId?.toString() ?: "",
                customerName = tracking.customerName.ifBlank { "Khach hang" },
                customerPhone = tracking.customerPhone,
                pickupAddress = tracking.storeAddress,
                deliveryAddress = tracking.shippingAddress,
                totalAmount = 0.0,
                status = mapApiStatusToOrderStatus(tracking.status),
                createdAt = tracking.assignedAt?.let { parseFlexibleDate(it) } ?: System.currentTimeMillis()
            )
        }
        displayOrderDetails()
    }
    
    private fun mapToOrder(detail: OrderDetailDto, tracking: TrackingSnapshot?, addressDto: com.qtifood.driver.data.remote.dto.AddressDto?): Order {
        val current = order
        val createdAtMillis = parseIsoDate(detail.createdAt)
        return Order(
            id = detail.id.toString(),
            customerName = addressDto?.receiver
                ?: tracking?.customerName?.takeIf { it.isNotBlank() }
                ?: current?.customerName
                ?: "Khach hang",
            customerPhone = addressDto?.phone
                ?: tracking?.customerPhone
                ?: current?.customerPhone
                ?: "",
            pickupAddress = tracking?.storeAddress?.ifBlank { current?.pickupAddress ?: "" }
                ?: current?.pickupAddress
                ?: "",
            deliveryAddress = addressDto?.address
                ?: tracking?.shippingAddress?.ifBlank { current?.deliveryAddress ?: "" }
                ?: current?.deliveryAddress
                ?: "",
            totalAmount = detail.totalAmount,
            status = mapApiStatusToOrderStatus(tracking?.status ?: detail.orderStatus),
            createdAt = createdAtMillis
        )
    }
    
    private fun mapApiStatusToOrderStatus(apiStatus: String): OrderStatus {
        return when (apiStatus.uppercase(Locale.getDefault())) {
            "PENDING" -> OrderStatus.PENDING
            "CONFIRMED", "ACCEPTED" -> OrderStatus.ACCEPTED
            "PREPARING" -> OrderStatus.ACCEPTED
            "READY_FOR_PICKUP" -> OrderStatus.PICKED_UP
            "SHIPPING" -> OrderStatus.DELIVERING
            "DELIVERED" -> OrderStatus.DELIVERED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }
    }
    
    private fun parseIsoDate(value: String): Long {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            isoFormat.parse(value)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseFlexibleDate(value: String): Long {
        return try {
            OffsetDateTime.parse(value).toInstant().toEpochMilli()
        } catch (_: Exception) {
            parseIsoDate(value)
        }
    }
    
    private fun displayOrderDetails() {
        val order = this.order ?: return
        
        binding.tvOrderId.text = "#${order.id}"
        updateStatusUI(order.status)
        
        binding.tvCustomerName.text = order.customerName
        binding.tvCustomerPhone.text = order.customerPhone.ifBlank { "—" }
        
        binding.tvPickupAddress.text = order.pickupAddress
        binding.tvDeliveryAddress.text = order.deliveryAddress
        
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        binding.tvTotalAmount.text = "${numberFormat.format(order.totalAmount)} đ"
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
        binding.tvCreatedDate.text = dateFormat.format(Date(order.createdAt))
        
        updateActionButtons(order.status)
    }
    
    private fun renderOrderItems() {
        orderItemsAdapter.submitItems(orderItems)
        val isEmpty = orderItems.isEmpty()
        binding.tvEmptyOrderItems.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvOrderItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun updateStatusUI(status: OrderStatus) {
        when (status) {
            OrderStatus.PENDING -> {
                binding.tvStatus.text = "Chờ xác nhận"
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_pending))
            }
            OrderStatus.ACCEPTED -> {
                binding.tvStatus.text = "Đã chấp nhận"
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_accepted))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_accepted))
            }
            OrderStatus.PICKED_UP -> {
                binding.tvStatus.text = "Đã lấy hàng"
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_picked_up))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_picked_up))
            }
            OrderStatus.DELIVERING -> {
                binding.tvStatus.text = "Đang giao"
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_delivering))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_delivering))
            }
            OrderStatus.DELIVERED -> {
                binding.tvStatus.text = "Đã giao"
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_delivered))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_delivered))
            }
            OrderStatus.CANCELLED -> {
                binding.tvStatus.text = "Đã hủy"
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_cancelled))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_cancelled))
            }
        }
    }
    
    private fun updateActionButtons(status: OrderStatus) {
        when (status) {
            OrderStatus.PENDING -> {
                binding.btnAccept.visibility = View.VISIBLE
                binding.btnReject.visibility = View.VISIBLE
                binding.btnPickedUp.visibility = View.GONE
                binding.btnDelivering.visibility = View.GONE
                binding.btnDelivered.visibility = View.GONE
            }
            OrderStatus.ACCEPTED -> {
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.btnPickedUp.visibility = View.VISIBLE
                binding.btnDelivering.visibility = View.GONE
                binding.btnDelivered.visibility = View.GONE
            }
            OrderStatus.PICKED_UP -> {
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.btnPickedUp.visibility = View.GONE
                binding.btnDelivering.visibility = View.VISIBLE
                binding.btnDelivered.visibility = View.GONE
            }
            OrderStatus.DELIVERING -> {
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.btnPickedUp.visibility = View.GONE
                binding.btnDelivering.visibility = View.GONE
                binding.btnDelivered.visibility = View.VISIBLE
            }
            else -> {
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.btnPickedUp.visibility = View.GONE
                binding.btnDelivering.visibility = View.GONE
                binding.btnDelivered.visibility = View.GONE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCallCustomer.setOnClickListener {
            val phone = order?.customerPhone
            if (!phone.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            }
        }
        
        binding.btnNavigatePickup.setOnClickListener {
            val address = order?.pickupAddress
            if (!address.isNullOrEmpty()) {
                openGoogleMaps(address)
            }
        }
        
        binding.btnNavigateDelivery.setOnClickListener {
            val address = order?.deliveryAddress
            if (!address.isNullOrEmpty()) {
                openGoogleMaps(address)
            }
        }
        
        binding.btnAccept.setOnClickListener {
            Toast.makeText(this, "Đã chấp nhận đơn hàng", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnReject.setOnClickListener {
            Toast.makeText(this, "Đã từ chối đơn hàng", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnPickedUp.setOnClickListener {
            Toast.makeText(this, "Đã lấy hàng", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnDelivering.setOnClickListener {
            Toast.makeText(this, "Đang giao hàng", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnDelivered.setOnClickListener {
            markOrderDelivered()
        }
    }
    
    private fun markOrderDelivered() {
        val orderId = activeOrderId
        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get current driver location
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Cần cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show()
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            
            lifecycleScope.launch {
                setLoading(true)
                val result = orderRepository.confirmDriverLocation(
                    orderId,
                    location.latitude,
                    location.longitude
                )
                result.onSuccess { detail ->
                    order = mapToOrder(detail, null, null)
                    displayOrderDetails()
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Đã xác nhận vị trí giao hàng thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToHome()
                }.onFailure { error ->
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Loi xac nhan giao hang: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                setLoading(false)
            }
        }
    }
    
    private fun openGoogleMaps(address: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
            )
            startActivity(browserIntent)
        }
    }
    
    private fun setLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun startLocationUpdates(orderId: Int) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        val request = LocationRequest.Builder(5000)
            .setMinUpdateIntervalMillis(3000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                orderTrackingDataSource.updateDriverLocation(
                    orderId.toLong(),
                    location.latitude,
                    location.longitude
                )
            }
        }
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            mainLooper
        )
    }
    
    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    private fun navigateToHome() {
        val intent = Intent(this, DriverHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        trackingListener?.let { listener ->
            trackingRef?.removeEventListener(listener)
        }
        trackingRef = null
        trackingListener = null
    }
}

data class TrackingSnapshot(
    val shippingAddress: String = "",
    val storeAddress: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val status: String = "",
    val assignedAt: String? = null,
    val shippingAddressId: Int? = null
)
