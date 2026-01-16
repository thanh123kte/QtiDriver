package com.qtifood.driver.presentation.income

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.qtifood.driver.databinding.ActivityIncomeStatsBinding
import com.qtifood.driver.domain.repository.DeliveryRepository
import com.qtifood.driver.domain.repository.DriverRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.util.Locale

class IncomeStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomeStatsBinding
    private val deliveryRepository: DeliveryRepository by inject()
    private val driverRepository: DriverRepository by inject()
    private val currencyFormatter = NumberFormat.getInstance(Locale("vi", "VN"))
    private val distanceFormatter = NumberFormat.getInstance(Locale("vi", "VN")).apply { maximumFractionDigits = 2 }
    private var currentPeriod = "daily"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupChips()
        loadStats(currentPeriod)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupChips() {
        binding.chipDaily.isChecked = true
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val period = when (checkedId) {
                binding.chipDaily.id -> "daily"
                binding.chipWeekly.id -> "weekly"
                binding.chipMonthly.id -> "monthly"
                else -> currentPeriod
            }
            if (period != currentPeriod) {
                currentPeriod = period
                loadStats(period)
            }
        }
    }

    private fun loadStats(period: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val driverId = driverRepository.getCurrentFirebaseUid()
            if (driverId.isNullOrEmpty()) {
                Toast.makeText(this@IncomeStatsActivity, "Khong tim thay tai xe", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            try {
                val result = deliveryRepository.getIncomeStats(driverId, period)
                result.onSuccess { stats ->
                    binding.tvPeriod.text = period.uppercase()
                    binding.tvTotalDeliveries.text = stats.totalDeliveries.toString()
                    binding.tvTotalIncome.text = "${currencyFormatter.format(stats.totalIncome)} đ"
                    binding.tvShippingFee.text = "${currencyFormatter.format(stats.totalShippingFee)} đ"
                    binding.tvDistance.text = "${distanceFormatter.format(stats.totalDistance)} km"
                    binding.tvAvgIncome.text = "${currencyFormatter.format(stats.averageIncomePerDelivery)} đ/đơn"
                    binding.tvAvgDistance.text = "${distanceFormatter.format(stats.averageDistancePerDelivery)} km/đơn"
                    binding.tvDateRange.text = "${stats.startDate ?: "-"} → ${stats.endDate ?: "-"}"
                }.onFailure { e ->
                    Toast.makeText(this@IncomeStatsActivity, "Loi tai thu nhap: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@IncomeStatsActivity, "Loi: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
