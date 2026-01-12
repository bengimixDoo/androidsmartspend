package com.example.smartspend2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.databinding.ItemTransactionBinding
import com.example.smartspend2.models.Transaction

class TransactionAdapter(private val transactions: List<Transaction>,
                         private val onLongClick: (Transaction, Int) -> Unit) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            binding.tvAmount.text = "${transaction.amount} Đ"
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = transaction.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
        val transaction = transactions[position]
        holder.bind(transaction)

        holder.itemView.setOnLongClickListener {
            onLongClick(transaction, position)
            true
        }
    }

    override fun getItemCount() = transactions.size
}

