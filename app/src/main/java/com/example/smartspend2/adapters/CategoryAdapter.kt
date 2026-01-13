package com.example.smartspend2.adapters

import android.view.LayoutInflater
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

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name

            if (category.isExpense) {
                // --- LOGIC CŨ CHO EXPENSE CATEGORY (GIỮ NGUYÊN) ---
                binding.tvAllocated.visibility = android.view.View.VISIBLE
                binding.tvRemaining.visibility = android.view.View.VISIBLE
                binding.progressBar.visibility = android.view.View.VISIBLE

                binding.tvAllocated.text = "Ngân sách: ${category.allocatedAmount}đ"
                binding.tvSpent.text = "Đã tiêu: ${category.spentAmount}đ"
                binding.tvRemaining.text = "Còn lại: ${category.allocatedAmount - category.spentAmount}đ"

                // Xử lý chia cho 0 để không bị crash
                val allocated = category.allocatedAmount.takeIf { it > 0f } ?: 1f
                val percent = (category.spentAmount / allocated * 100).toInt()

                binding.progressBar.progress = percent.coerceAtMost(100)

            } else {
                // --- LOGIC MỚI CHO INCOME CATEGORY ---
                // 1. Ẩn các view không cần thiết
                binding.tvAllocated.visibility = android.view.View.GONE
                binding.tvRemaining.visibility = android.view.View.GONE
                binding.progressBar.visibility = android.view.View.GONE

                // 2. Hiển thị tổng thu nhập một cách rõ ràng
                binding.tvSpent.text = "Đã thu về: ${category.spentAmount}đ"
            }

            // Set long press listener here
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
