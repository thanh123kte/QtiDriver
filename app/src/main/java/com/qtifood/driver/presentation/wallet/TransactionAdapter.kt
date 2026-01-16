package com.qtifood.driver.presentation.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qtifood.driver.R
import com.qtifood.driver.domain.model.Transaction
import com.qtifood.driver.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
        holder.itemView.setOnClickListener {
            TransactionDetailActivity.start(holder.itemView.context, transaction)
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconCard: CardView = itemView.findViewById(R.id.cvTransactionIcon)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivTransactionIcon)
        private val tvType: TextView = itemView.findViewById(R.id.tvTransactionType)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTransactionDate)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvTransactionDescription)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)

        fun bind(transaction: Transaction) {
            val isDeliveryIncome = transaction.referenceType.equals("DELIVERY_INCOME", ignoreCase = true)
            when (transaction.type) {
                TransactionType.DEPOSIT -> {
                    tvType.text = "Nạp tiền"
                    ivIcon.setImageResource(R.drawable.ic_topup)
                    iconCard.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                TransactionType.WITHDRAW -> {
                    tvType.text = "Rút tiền"
                    ivIcon.setImageResource(R.drawable.ic_withdraw)
                    iconCard.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_orange_light))
                }
                TransactionType.PAYMENT -> {
                    tvType.text = "Thanh toán"
                    ivIcon.setImageResource(R.drawable.ic_transaction)
                    iconCard.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_blue_light))
                }
                TransactionType.REFUND -> {
                    tvType.text = "Hoàn tiền"
                    ivIcon.setImageResource(R.drawable.ic_topup)
                    iconCard.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_purple))
                }
                TransactionType.EARN -> {
                    tvType.text = if (isDeliveryIncome) "Thu nhập giao hàng" else "Thu nhập khác"
                    ivIcon.setImageResource(R.drawable.ic_topup)
                    iconCard.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                TransactionType.DELIVERY_INCOME -> {
                    tvType.text = "Thu nhập giao hàng"
                    ivIcon.setImageResource(R.drawable.ic_topup)
                    iconCard.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                TransactionType.MANUAL_INCOME -> {
                    tvType.text = "Thu nhập giao hàng"
                    ivIcon.setImageResource(R.drawable.ic_topup)
                    iconCard.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
            }

            tvDate.text = formatDate(transaction.createdAt)

            if (!transaction.description.isNullOrEmpty()) {
                tvDescription.visibility = View.VISIBLE
                tvDescription.text = transaction.description
            } else {
                tvDescription.visibility = View.GONE
            }

            val formattedAmount = formatCurrency(transaction.amount)
            when (transaction.type) {
                TransactionType.DEPOSIT, TransactionType.REFUND, TransactionType.EARN, TransactionType.DELIVERY_INCOME, TransactionType.MANUAL_INCOME -> {
                    tvAmount.text = "+$formattedAmount"
                    tvAmount.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                TransactionType.WITHDRAW, TransactionType.PAYMENT -> {
                    tvAmount.text = "-$formattedAmount"
                    tvAmount.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateString)
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }

        private fun formatCurrency(amount: Double): String {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return "${formatter.format(amount)}đ"
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
