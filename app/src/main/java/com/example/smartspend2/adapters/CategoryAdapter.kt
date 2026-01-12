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
            binding.tvAllocated.text = "Ngân sách: ${category.allocatedAmount} Đ"
            binding.tvSpent.text = if (category.isExpense) "Đã chi: ${category.spentAmount} Đ" else "Đã nhận: ${category.spentAmount} Đ"
            binding.tvRemaining.text = if (category.isExpense)
                "Còn: ${category.allocatedAmount - category.spentAmount} Đ"
            else
                "Còn: ${category.spentAmount - category.allocatedAmount} Đ"

            val percent = if (category.isExpense)
                (category.spentAmount / category.allocatedAmount * 100).toInt()
            else
                (category.allocatedAmount / (category.spentAmount.takeIf { it > 0 } ?: 1f) * 100).toInt()

            binding.progressBar.progress = percent.coerceAtMost(100)
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
