package com.qtifood.driver.presentation.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qtifood.driver.data.remote.dto.OrderItemDto
import com.qtifood.driver.databinding.ItemOrderProductBinding
import java.text.NumberFormat
import java.util.Locale

class OrderItemsAdapter : RecyclerView.Adapter<OrderItemsAdapter.ItemViewHolder>() {

    private val items = mutableListOf<OrderItemDto>()
    private val currencyFormatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    fun submitItems(newItems: List<OrderItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(
        private val binding: ItemOrderProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderItemDto) {
            binding.tvProductName.text = item.productName
            binding.tvQuantity.text = "x${item.quantity}"
            binding.tvPrice.text = "${currencyFormatter.format(item.price)} đ"
            binding.tvTotalPrice.text = "${currencyFormatter.format(item.totalPrice)} đ"
        }
    }
}
