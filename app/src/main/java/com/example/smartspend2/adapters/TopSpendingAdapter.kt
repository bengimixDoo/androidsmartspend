package com.example.smartspend2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.R
import java.text.DecimalFormat

data class TopSpendingItem(val category: String, val amount: Float)

class TopSpendingAdapter(private val items: List<TopSpendingItem>) :
    RecyclerView.Adapter<TopSpendingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_spending, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRank.text = "${position + 1}."
        holder.tvCategoryName.text = item.category

        val formatter = DecimalFormat("#,###")
        holder.tvAmount.text = "${formatter.format(item.amount)} đ"
    }

    override fun getItemCount(): Int = items.size
}