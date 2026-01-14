// Trong file D:/BT/Android_Studio/androidsmartspend/app/src/main/java/com/example/smartspend2/adapters/TransactionAdapter.kt

package com.example.smartspend2.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.databinding.ItemTransactionBinding
import com.example.smartspend2.models.Transaction
import java.text.DecimalFormat

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onLongClick: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    // Tạo một đối tượng DecimalFormat để tái sử dụng
    private val numberFormat = DecimalFormat("#,###")

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = transaction.date

            // === SỬA Ở ĐÂY: DÙNG numberFormat.format() và đặt màu ===
            val formattedAmount = numberFormat.format(transaction.amount)
            if (transaction.isExpense) {
                binding.tvAmount.text = "- ${formattedAmount}đ"
                binding.tvAmount.setTextColor(Color.parseColor("#D32F2F")) // Màu đỏ cho chi tiêu
            } else {
                binding.tvAmount.text = "+ ${formattedAmount}đ"
                binding.tvAmount.setTextColor(Color.parseColor("#388E3C")) // Màu xanh cho thu nhập
            }

            binding.root.setOnLongClickListener {
                onLongClick(transaction, adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    // Hàm để cập nhật dữ liệu từ bên ngoài (nếu cần)
    fun updateData(newTransactions: List<Transaction>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }
}
