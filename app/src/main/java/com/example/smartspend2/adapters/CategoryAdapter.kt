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

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name

            if (category.isExpense) {
                binding.tvAllocated.visibility = View.VISIBLE
                binding.tvRemaining.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE

                binding.tvAllocated.text = "Ngân sách: ${category.allocatedAmount}đ"
                binding.tvSpent.text = "Đã tiêu: ${category.spentAmount}đ"
                binding.tvRemaining.text = "Còn lại: ${category.allocatedAmount - category.spentAmount}đ"

                // Avoid division by zero
                val allocated = category.allocatedAmount.takeIf { it > 0f } ?: 1f
                val percent = (category.spentAmount / allocated * 100).toInt()
                binding.progressBar.progress = percent.coerceAtMost(100)

            } else {
                // Income category — hide expense-specific views
                binding.tvAllocated.visibility = View.GONE
                binding.tvRemaining.visibility = View.GONE
                binding.progressBar.visibility = View.GONE

                binding.tvSpent.text = "Đã thu về: ${category.spentAmount}đ"
            }

            // Set long press listener
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
