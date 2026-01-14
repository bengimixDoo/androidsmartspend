package com.example.smartspend2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.databinding.ItemCategoryBinding
import com.example.smartspend2.models.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onLongClick: (Category, Int) -> Unit  // Long press listener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val numberFormat = java.text.DecimalFormat("#,###")

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name

            if (category.isExpense) {
                binding.tvAllocated.visibility = View.VISIBLE
                binding.tvRemaining.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE

                // === SỬA Ở ĐÂY: DÙNG numberFormat.format() ===
                binding.tvAllocated.text = "Ngân sách: ${numberFormat.format(category.allocatedAmount)}đ"
                binding.tvSpent.text = "Đã tiêu: ${numberFormat.format(category.spentAmount)}đ"
                val remaining = category.allocatedAmount - category.spentAmount
                binding.tvRemaining.text = "Còn lại: ${numberFormat.format(remaining)}đ"

                val allocated = category.allocatedAmount.takeIf { it > 0f } ?: 1f
                val percent = (category.spentAmount / allocated * 100).toInt()
                binding.progressBar.progress = percent.coerceAtMost(100)

            } else {
                binding.tvAllocated.visibility = View.GONE
                binding.tvRemaining.visibility = View.GONE
                binding.progressBar.visibility = View.GONE

                // === SỬA Ở ĐÂY: DÙNG numberFormat.format() ===
                binding.tvSpent.text = "Đã thu về: ${numberFormat.format(category.spentAmount)}đ"
            }

            binding.root.setOnLongClickListener {
                onLongClick(category, adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size
}
