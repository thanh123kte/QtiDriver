package com.qtifood.driver.presentation.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.qtifood.driver.R
import com.qtifood.driver.domain.model.Transaction
import com.qtifood.driver.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TransactionDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_TRANSACTION_ID = "transaction_id"
        private const val EXTRA_WALLET_ID = "wallet_id"
        private const val EXTRA_AMOUNT = "amount"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_BALANCE_BEFORE = "balance_before"
        private const val EXTRA_BALANCE_AFTER = "balance_after"
        private const val EXTRA_DESCRIPTION = "description"
        private const val EXTRA_REFERENCE_ID = "reference_id"
        private const val EXTRA_REFERENCE_TYPE = "reference_type"
        private const val EXTRA_CREATED_AT = "created_at"

        fun start(context: Context, transaction: Transaction) {
            val intent = Intent(context, TransactionDetailActivity::class.java).apply {
                putExtra(EXTRA_TRANSACTION_ID, transaction.id)
                putExtra(EXTRA_WALLET_ID, transaction.walletId)
                putExtra(EXTRA_AMOUNT, transaction.amount)
                putExtra(EXTRA_TYPE, transaction.type.name)
                putExtra(EXTRA_BALANCE_BEFORE, transaction.balanceBefore)
                putExtra(EXTRA_BALANCE_AFTER, transaction.balanceAfter)
                putExtra(EXTRA_DESCRIPTION, transaction.description)
                putExtra(EXTRA_REFERENCE_ID, transaction.referenceId)
                putExtra(EXTRA_REFERENCE_TYPE, transaction.referenceType)
                putExtra(EXTRA_CREATED_AT, transaction.createdAt)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var cvTransactionIcon: CardView
    private lateinit var ivTransactionIcon: ImageView
    private lateinit var tvTransactionType: TextView
    private lateinit var tvTransactionAmount: TextView
    private lateinit var tvTransactionId: TextView
    private lateinit var tvBalanceBefore: TextView
    private lateinit var tvBalanceAfter: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvReferenceType: TextView
    private lateinit var tvReferenceId: TextView
    private lateinit var layoutReferenceType: View
    private lateinit var layoutReferenceId: View
    private lateinit var dividerReferenceType: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        initViews()
        setupToolbar()
        displayTransactionDetails()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        cvTransactionIcon = findViewById(R.id.cvTransactionIcon)
        ivTransactionIcon = findViewById(R.id.ivTransactionIcon)
        tvTransactionType = findViewById(R.id.tvTransactionType)
        tvTransactionAmount = findViewById(R.id.tvTransactionAmount)
        tvTransactionId = findViewById(R.id.tvTransactionId)
        tvBalanceBefore = findViewById(R.id.tvBalanceBefore)
        tvBalanceAfter = findViewById(R.id.tvBalanceAfter)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)
        tvDescription = findViewById(R.id.tvDescription)
        tvReferenceType = findViewById(R.id.tvReferenceType)
        tvReferenceId = findViewById(R.id.tvReferenceId)
        layoutReferenceType = findViewById(R.id.layoutReferenceType)
        layoutReferenceId = findViewById(R.id.layoutReferenceId)
        dividerReferenceType = findViewById(R.id.dividerReferenceType)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun displayTransactionDetails() {
        val transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, 0)
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val typeString = intent.getStringExtra(EXTRA_TYPE) ?: "PAYMENT"
        val type = TransactionType.valueOf(typeString)
        val balanceBefore = intent.getDoubleExtra(EXTRA_BALANCE_BEFORE, 0.0)
        val balanceAfter = intent.getDoubleExtra(EXTRA_BALANCE_AFTER, 0.0)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION)
        val referenceId = intent.getStringExtra(EXTRA_REFERENCE_ID)
        val referenceType = intent.getStringExtra(EXTRA_REFERENCE_TYPE)
        val createdAt = intent.getStringExtra(EXTRA_CREATED_AT) ?: ""
        val isDeliveryIncome = referenceType.equals("DELIVERY_INCOME", ignoreCase = true)

        when (type) {
            TransactionType.DEPOSIT -> {
                tvTransactionType.text = "Nạp tiền"
                ivTransactionIcon.setImageResource(R.drawable.ic_topup)
                cvTransactionIcon.setCardBackgroundColor(getColor(android.R.color.holo_green_light))
                tvTransactionAmount.setTextColor(getColor(android.R.color.holo_green_dark))
                tvTransactionAmount.text = "+${formatCurrency(amount)}"
            }
            TransactionType.WITHDRAW -> {
                tvTransactionType.text = "Rút tiền"
                ivTransactionIcon.setImageResource(R.drawable.ic_withdraw)
                cvTransactionIcon.setCardBackgroundColor(getColor(android.R.color.holo_orange_light))
                tvTransactionAmount.setTextColor(getColor(android.R.color.holo_red_dark))
                tvTransactionAmount.text = "-${formatCurrency(amount)}"
            }
            TransactionType.PAYMENT -> {
                tvTransactionType.text = "Thanh toán"
                ivTransactionIcon.setImageResource(R.drawable.ic_transaction)
                cvTransactionIcon.setCardBackgroundColor(getColor(android.R.color.holo_blue_light))
                tvTransactionAmount.setTextColor(getColor(android.R.color.holo_red_dark))
                tvTransactionAmount.text = "-${formatCurrency(amount)}"
            }
            TransactionType.REFUND -> {
                tvTransactionType.text = "Hoàn tiền"
                ivTransactionIcon.setImageResource(R.drawable.ic_topup)
                cvTransactionIcon.setCardBackgroundColor(getColor(android.R.color.holo_purple))
                tvTransactionAmount.setTextColor(getColor(android.R.color.holo_green_dark))
                tvTransactionAmount.text = "+${formatCurrency(amount)}"
            }
            TransactionType.EARN -> {
                tvTransactionType.text = if (isDeliveryIncome) "Thu nhập giao hàng" else "Thu nhập khác"
                ivTransactionIcon.setImageResource(R.drawable.ic_topup)
                cvTransactionIcon.setCardBackgroundColor(getColor(android.R.color.holo_green_light))
                tvTransactionAmount.setTextColor(getColor(android.R.color.holo_green_dark))
                tvTransactionAmount.text = "+${formatCurrency(amount)}"
            }
            TransactionType.DELIVERY_INCOME -> {
                tvTransactionType.text = "Thu nhập giao hàng"
                ivTransactionIcon.setImageResource(R.drawable.ic_topup)
                cvTransactionIcon.setCardBackgroundColor(getColor(android.R.color.holo_green_light))
                tvTransactionAmount.setTextColor(getColor(android.R.color.holo_green_dark))
                tvTransactionAmount.text = "+${formatCurrency(amount)}"
            }
            TransactionType.MANUAL_INCOME -> {
                tvTransactionType.text = "Thu nhập giao hàng"
                ivTransactionIcon.setImageResource(R.drawable.ic_topup)
                cvTransactionIcon.setCardBackgroundColor(getColor(android.R.color.holo_green_light))
                tvTransactionAmount.setTextColor(getColor(android.R.color.holo_green_dark))
                tvTransactionAmount.text = "+${formatCurrency(amount)}"
            }
        }

        tvTransactionId.text = "#$transactionId"
        tvBalanceBefore.text = formatCurrency(balanceBefore)
        tvBalanceAfter.text = formatCurrency(balanceAfter)
        tvCreatedAt.text = formatDate(createdAt)

        if (!description.isNullOrEmpty()) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = description
        } else {
            tvDescription.visibility = View.GONE
        }

        if (!referenceType.isNullOrEmpty()) {
            layoutReferenceType.visibility = View.VISIBLE
            dividerReferenceType.visibility = View.VISIBLE
            tvReferenceType.text = referenceType
        } else {
            layoutReferenceType.visibility = View.GONE
            dividerReferenceType.visibility = View.GONE
        }

        if (!referenceId.isNullOrEmpty()) {
            layoutReferenceId.visibility = View.VISIBLE
            tvReferenceId.text = referenceId
        } else {
            layoutReferenceId.visibility = View.GONE
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        return "${formatter.format(amount)}đ"
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
}
