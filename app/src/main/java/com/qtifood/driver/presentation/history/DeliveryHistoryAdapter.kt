package com.qtifood.driver.presentation.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qtifood.driver.R
import com.qtifood.driver.domain.model.Delivery
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DeliveryHistoryAdapter :
    ListAdapter<Delivery, DeliveryHistoryAdapter.ViewHolder>(Diff()) {
    
    var onItemClick: ((Delivery) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val tvCompletedAt: TextView = itemView.findViewById(R.id.tvCompletedAt)

        fun bind(delivery: Delivery) {
            tvOrderId.text = "#${delivery.orderId}"
            tvStatus.text = delivery.status
            tvDistance.text = "${formatDistance(delivery.distanceKm)} km"
            tvCompletedAt.text = formatDate(delivery.completedAt ?: delivery.startedAt ?: "")
        }

        private fun formatDistance(distanceKm: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            formatter.maximumFractionDigits = 2
            return formatter.format(distanceKm)
        }

        private fun formatDate(value: String): String {
            return try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = input.parse(value) ?: Date()
                val output = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
                output.format(date)
            } catch (e: Exception) {
                value
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<Delivery>() {
        override fun areItemsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
            return oldItem == newItem
        }
    }
}
