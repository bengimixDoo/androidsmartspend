// Dán toàn bộ nội dung này vào file D:/BT/Android_Studio/androidsmartspend/app/src/main/java/com/example/smartspend2/CategoriesFragment.kt
package com.example.smartspend2

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast // THÊM IMPORT NÀY
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.adapters.CategoryAdapter
import com.example.smartspend2.models.Category

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

        dbHelper = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.rvCategories)
        recyclerView.layoutManager = LinearLayoutManager(context)
        categoryAdapter = CategoryAdapter(categories) { category, _ ->
            showDeleteCategoryDialog(category)
        }
        recyclerView.adapter = categoryAdapter

        // Load và cập nhật dữ liệu khi view được tạo
        loadAndSyncCategories()

        val fabAddCategory: View = view.findViewById(R.id.fabAddCategory)
        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun loadAndSyncCategories() {
        categories.clear()
        categories.addAll(dbHelper.getAllCategories())

        val spendingMap = dbHelper.calculateCategorySpending()
        val incomeMap = dbHelper.calculateCategoryIncome()

        categories.forEach { category ->
            if (category.isExpense) {
                category.spentAmount = spendingMap[category.name] ?: 0f
            } else {
                category.spentAmount = incomeMap[category.name] ?: 0f
            }
        }
        categoryAdapter.notifyDataSetChanged()
    }

    // =====================================================================
    // HÀM ĐÃ ĐƯỢC SỬA LẠI ĐẦY ĐỦ
    // =====================================================================
    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val etAllocatedAmount = dialogView.findViewById<EditText>(R.id.etAllocatedAmount)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rbExpense)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)

        rbExpense.isChecked = true // Mặc định là Expense
        etAllocatedAmount.visibility = View.VISIBLE // Mặc định hiển thị ngân sách

        rbExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etAllocatedAmount.visibility = View.VISIBLE
            }
        }
        rbIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etAllocatedAmount.visibility = View.GONE
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSubmit.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            val isExpense = rbExpense.isChecked
            // Nếu là income, ngân sách mặc định là 0
            val allocatedAmount = if (isExpense) etAllocatedAmount.text.toString().toFloatOrNull() else 0f

            if (name.isEmpty() || (isExpense && allocatedAmount == null)) {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            } else {
                val newCategory = Category(name, allocatedAmount!!, 0f, isExpense)
                val result = dbHelper.insertCategory(newCategory)

                if (result != -1L) {
                    // Load lại toàn bộ để đồng bộ chính xác
                    loadAndSyncCategories()
                    dialog.dismiss()
                } else {
                    etCategoryName.error = "Category already exists"
                }
            }
        }
        dialog.show()
    }

    // =====================================================================
    // HÀM ĐÃ ĐƯỢC SỬA LẠI ĐẦY ĐỦ
    // =====================================================================
    private fun showDeleteCategoryDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete the '${category.name}' category?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteCategory(category.name)
                // Load lại toàn bộ để đồng bộ chính xác
                loadAndSyncCategories()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadAndSyncCategories()
    }
}
