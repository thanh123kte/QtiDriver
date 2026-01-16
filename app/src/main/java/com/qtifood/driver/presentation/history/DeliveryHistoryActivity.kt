package com.qtifood.driver.presentation.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.qtifood.driver.presentation.order.OrderDetailActivity
import com.qtifood.driver.databinding.ActivityDeliveryHistoryBinding
import com.qtifood.driver.domain.repository.DeliveryRepository
import com.qtifood.driver.domain.repository.DriverRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DeliveryHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryHistoryBinding
    private val deliveryRepository: DeliveryRepository by inject()
    private val driverRepository: DriverRepository by inject()
    private val adapter = DeliveryHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupList()
        loadHistory()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupList() {
        binding.rvDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvDeliveries.adapter = adapter
        adapter.onItemClick = { delivery ->
            val intent = Intent(this, DeliveryHistoryDetailActivity::class.java).apply {
                putExtra(DeliveryHistoryDetailActivity.EXTRA_DELIVERY, delivery)
            }
            startActivity(intent)
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val driverId = driverRepository.getCurrentFirebaseUid()
            if (driverId.isNullOrEmpty()) {
                Toast.makeText(this@DeliveryHistoryActivity, "Khong tim thay tai xe", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            val result = deliveryRepository.getDeliveriesByDriver(driverId)
            result.onSuccess {
                adapter.submitList(it)
                binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure { e ->
                Toast.makeText(this@DeliveryHistoryActivity, "Loi tai lich su: ${e.message}", Toast.LENGTH_LONG).show()
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}
