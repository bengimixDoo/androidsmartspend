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
            binding.tvAllocated.text = "Allocated: Rs. ${category.allocatedAmount}"
            binding.tvSpent.text = if (category.isExpense) "Spent: Rs. ${category.spentAmount}" else "Earned: Rs. ${category.spentAmount}"
            binding.tvRemaining.text = if (category.isExpense)
                "Remaining: Rs. ${category.allocatedAmount - category.spentAmount}"
            else
                "Remaining: Rs. ${category.spentAmount - category.allocatedAmount}"

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
