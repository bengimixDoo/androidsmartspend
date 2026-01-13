package com.example.smartspend2.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.R
import java.text.DecimalFormat

// Model dữ liệu riêng cho báo cáo này
data class CategoryReportItem(
    val categoryName: String,
    val totalAmount: Float,
    val transactionCount: Int
)

class CategoryReportAdapter(
    private val items: List<CategoryReportItem>,
    private val onItemClick: (CategoryReportItem) -> Unit // Sự kiện click để xem chi tiết sau này
) : RecyclerView.Adapter<CategoryReportAdapter.ViewHolder>() {

    // Danh sách màu ngẫu nhiên cho đẹp
    private val colors = listOf(
        "#FF6B6B", "#4ECDC4", "#FFD166", "#118AB2", "#06D6A0", "#EF476F", "#264653"
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvIcon)
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvCount: TextView = view.findViewById(R.id.tvTransactionCount)
        val tvAmount: TextView = view.findViewById(R.id.tvTotalAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.categoryName
        holder.tvCount.text = "${item.transactionCount} giao dịch"

        val formatter = DecimalFormat("#,###")
        holder.tvAmount.text = "${formatter.format(item.totalAmount)} đ"

        // Set chữ cái đầu làm Icon
        if (item.categoryName.isNotEmpty()) {
            holder.tvIcon.text = item.categoryName.first().toString().uppercase()
        }

        // Set màu background ngẫu nhiên theo tên
        val color = Color.parseColor(colors[position % colors.size])
        val background = holder.tvIcon.background as GradientDrawable
        background.setColor(color)

        // Bắt sự kiện click
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}