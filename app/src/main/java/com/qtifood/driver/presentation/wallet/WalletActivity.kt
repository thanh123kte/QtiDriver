package com.qtifood.driver.presentation.wallet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.qtifood.driver.R
import com.qtifood.driver.databinding.ActivityWalletBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.NumberFormat
import java.util.*

class WalletActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWalletBinding
    private lateinit var transactionAdapter: TransactionAdapter
    
    private val viewModel: WalletViewModel by viewModel {
        parametersOf(FirebaseAuth.getInstance().currentUser?.uid ?: "")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(this@WalletActivity)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnTopUp.setOnClickListener {
            showTopUpDialog()
        }
        
        binding.btnWithdraw.setOnClickListener {
            Toast.makeText(this, "Chức năng rút tiền đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnHistory.setOnClickListener {
            // Already showing history, maybe scroll to top
            binding.rvTransactions.smoothScrollToPosition(0)
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadWalletData()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Update loading state
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Update wallet balance
                state.wallet?.let { wallet ->
                    binding.tvBalance.text = formatCurrency(wallet.balance)
                }
                
                // Update transactions
                if (state.transactions.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvTransactions.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvTransactions.visibility = View.VISIBLE
                    transactionAdapter.submitList(state.transactions)
                }
                
                // Show error
                state.errorMessage?.let { message ->
                    Toast.makeText(this@WalletActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
                
                // Show QR code dialog when top up successful
                state.topUpResult?.let { topUp ->
                    showQRCodeDialog(topUp)
                    viewModel.clearTopUpResult()
                }
            }
        }
    }
    
    private fun showTopUpDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_topup_amount)
        
        val etAmount = dialog.findViewById<TextInputEditText>(R.id.etAmount)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnTopUp = dialog.findViewById<Button>(R.id.btnTopUp)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnTopUp.setOnClickListener {
            val amountStr = etAmount.text?.toString()
            if (amountStr.isNullOrEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount < 10000) {
                Toast.makeText(this, "Số tiền tối thiểu là 10,000 VNĐ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.topUp(amount)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showQRCodeDialog(topUp: com.qtifood.driver.domain.model.TopUp) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_qrcode)
        
        val tvAmount = dialog.findViewById<TextView>(R.id.tvAmount)
        val tvTransactionId = dialog.findViewById<TextView>(R.id.tvTransactionId)
        val ivQRCode = dialog.findViewById<ImageView>(R.id.ivQRCode)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        
        tvAmount.text = "Số tiền: ${formatCurrency(topUp.amount)}"
        tvTransactionId.text = topUp.providerTransactionId
        
        // Build QR code URL
        val accountNumber = "0606914301919" // Replace with actual account
        val bankCode = "MBBANK" // Replace with actual bank
        val qrUrl = "https://qr.sepay.vn/img?acc=$accountNumber&bank=$bankCode&amount=${topUp.amount.toInt()}&des=${topUp.providerTransactionId}"
        
        // Load QR code
        Glide.with(this)
            .load(qrUrl)
            .into(ivQRCode)
        
        btnClose.setOnClickListener {
            dialog.dismiss()
            // Reload wallet data after closing
            viewModel.loadWalletData()
        }
        
        dialog.show()
    }
    
    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        return "${formatter.format(amount)} VNĐ"
    }
}
