package com.qtifood.driver.presentation.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qtifood.driver.databinding.ActivityDeliveryHistoryDetailBinding
import com.qtifood.driver.domain.model.Delivery

class DeliveryHistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryHistoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val delivery = intent.getSerializableExtra(EXTRA_DELIVERY) as? Delivery
        delivery?.let { showDetail(it) } ?: finish()
    }

    private fun showDetail(delivery: Delivery) {
        binding.tvOrderId.text = "#${delivery.orderId}"
        binding.tvStatus.text = delivery.status
        binding.tvStore.text = delivery.storeName ?: "Cua hang"
        binding.tvCustomer.text = delivery.customerName ?: "Khach hang"
        binding.tvShippingAddress.text = delivery.shippingAddress ?: "-"
        binding.tvDistance.text = "${formatDistance(delivery.distanceKm)} km"
        binding.tvGoodsAmount.text = formatCurrency(delivery.goodsAmount)
        binding.tvShippingFee.text = formatCurrency(delivery.shippingFee)
        binding.tvDriverIncome.text = formatCurrency(delivery.driverIncome)
        binding.tvPaymentMethod.text = delivery.paymentMethod
        binding.tvStartedAt.text = delivery.startedAt ?: "-"
        binding.tvCompletedAt.text = delivery.completedAt ?: "-"
    }

    private fun formatDistance(value: Double): String {
        return String.format("%.2f", value)
    }

    private fun formatCurrency(value: Double): String {
        return String.format("%,.0f đ", value)
    }

    companion object {
        const val EXTRA_DELIVERY = "extra_delivery"
    }
}
