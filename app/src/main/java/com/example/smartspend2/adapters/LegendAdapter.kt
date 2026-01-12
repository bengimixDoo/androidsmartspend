// File: app/src/main/java/com/example/smartspend2/adapters/LegendAdapter.kt
package com.example.smartspend2.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.R

// Data class để giữ thông tin cho mỗi mục legend
data class LegendItem(val color: Int, val label: String)

class LegendAdapter(private val items: List<LegendItem>) : RecyclerView.Adapter<LegendAdapter.LegendViewHolder>() {

    class LegendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorIndicator: View = view.findViewById(R.id.colorIndicator)
        val categoryName: TextView = view.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_legend, parent, false)
        return LegendViewHolder(view)
    }

    override fun onBindViewHolder(holder: LegendViewHolder, position: Int) {
        val item = items[position]

        // Đổi màu cho chấm tròn
        (holder.colorIndicator.background as? GradientDrawable)?.setColor(item.color)
        holder.categoryName.text = item.label
    }

    override fun getItemCount() = items.size
}
