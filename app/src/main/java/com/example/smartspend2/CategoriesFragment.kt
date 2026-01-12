package com.example.smartspend2

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.R
import com.example.smartspend2.adapters.CategoryAdapter
import com.example.smartspend2.models.Category
import com.example.smartspend2.DatabaseHelper

class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        recyclerView = view.findViewById(R.id.rvCategories)
        recyclerView.layoutManager = LinearLayoutManager(context)

        categoryAdapter = CategoryAdapter(categories) { category, position ->
            showDeleteCategoryDialog(category, position)
        }

        recyclerView.adapter = categoryAdapter
        // Load saved categories
        loadCategories()
        // sync with transactions
        // Sync spending from transactions
        val spendingMap = dbHelper.calculateCategorySpending()
        categories.forEach { category ->
            category.spentAmount = spendingMap[category.name] ?: 0f
        }
        categoryAdapter.notifyDataSetChanged()



        // Floating Action Button for Adding Category
        val fabAddCategory: View = view.findViewById(R.id.fabAddCategory)
        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(dbHelper.getAllCategories())
        categoryAdapter.notifyDataSetChanged()
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val etAllocatedAmount = dialogView.findViewById<EditText>(R.id.etAllocatedAmount)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rbExpense)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Handle Category Addition
        btnSubmit.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            val allocatedAmount = etAllocatedAmount.text.toString().toFloatOrNull()
            val isExpense = rbExpense.isChecked

            if (name.isEmpty() || allocatedAmount == null) {
                etCategoryName.error = if (name.isEmpty()) "Enter category name" else null
                etAllocatedAmount.error = if (allocatedAmount == null) "Enter valid amount" else null
            } else {
                // Add new category to the database
                val newCategory = Category(name, allocatedAmount, 0f, isExpense)
                val result = dbHelper.insertCategory(newCategory)

                if (result != -1L) {
                    categories.add(newCategory)
                    categoryAdapter.notifyDataSetChanged()
                    dialog.dismiss()
                } else {
                    etCategoryName.error = "Category already exists"
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteCategoryDialog(category: Category, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete the '${category.name}' category?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteCategory(category.name)
                categories.removeAt(position)
                categoryAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
