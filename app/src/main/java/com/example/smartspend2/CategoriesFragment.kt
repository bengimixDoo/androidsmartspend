// Dán toàn bộ nội dung này vào file D:/BT/Android_Studio/androidsmartspend/app/src/main/java/com/example/smartspend2/CategoriesFragment.kt
package com.example.smartspend2

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.adapters.CategoryAdapter
import com.example.smartspend2.models.Category

/**
 * Fragment quản lý danh mục (Categories).
 *
 * Cung cấp các chức năng:
 * - Hiển thị danh sách danh mục (bao gồm cả mặc định và tự tạo).
 * - Thêm danh mục mới với ngân sách dự kiến.
 * - Xóa danh mục (kèm logic di chuyển dữ liệu giao dịch cũ).
 */
class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()
    private lateinit var dbHelper: DatabaseHelper

    /**
     * Khởi tạo View cho Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    /**
     * Được gọi sau khi View đã được khởi tạo.
     * Thiết lập RecyclerView, Adapter và các sự kiện click.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.rvCategories)
        recyclerView.layoutManager = LinearLayoutManager(context)
        categoryAdapter = CategoryAdapter(categories) { category, _ ->
            showDeleteCategoryDialog(category)
        }
        recyclerView.adapter = categoryAdapter

        loadAndSyncCategories()

        val fabAddCategory: View = view.findViewById(R.id.fabAddCategory)
        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    /**
     * Tải danh sách danh mục từ Database, đồng bộ số liệu và cập nhật UI.
     *
     * Quy trình:
     * 1. Lấy toàn bộ danh mục.
     * 2. Tính toán tổng thu/chi thực tế cho từng danh mục.
     * 3. Lọc hiển thị: Chỉ hiện các danh mục đang hoạt động (có giao dịch/ngân sách hoặc do người dùng tạo).
     */
    private fun loadAndSyncCategories() {
        categories.clear()
        val allCategories = dbHelper.getAllCategories()
        val spendingMap = dbHelper.calculateCategorySpending()
        val incomeMap = dbHelper.calculateCategoryIncome()

        allCategories.forEach { category ->
            if (category.isExpense) {
                category.spentAmount = spendingMap[category.name] ?: 0f
            } else {
                category.spentAmount = incomeMap[category.name] ?: 0f
            }
        }

        val activeCategories = allCategories.filter { category ->
            category.key.isNullOrEmpty() || category.allocatedAmount > 0f || category.spentAmount > 0f
        }

        categories.addAll(activeCategories)
        categoryAdapter.notifyDataSetChanged()
    }

    /**
     * Hiển thị hộp thoại thêm danh mục mới.
     */
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

            if (name.isEmpty() || allocatedAmount == null) {
                etCategoryName.error = if (name.isEmpty()) "Nhập tên danh mục" else null
                etAllocatedAmount.error = if (allocatedAmount == null) "Nhập số tiền" else null
            } else {
                val newCategory = Category(
                    name = name,
                    allocatedAmount = allocatedAmount!!,
                    spentAmount = 0f,
                    isExpense = isExpense
                )
                val result = dbHelper.insertCategory(newCategory)

                if (result != -1L) {
                    loadAndSyncCategories()
                    dialog.dismiss()
                } else {
                    etCategoryName.error = "Danh mục đã tồn tại"
                }
            }
        }
        dialog.show()
    }

    /**
     * Hiển thị hộp thoại xác nhận xóa danh mục.
     *
     * - Nếu là danh mục mặc định: Không cho phép xóa.
     * - Nếu là danh mục người dùng: Yêu cầu xác nhận và di chuyển giao dịch cũ sang mục "Khác".
     */
    private fun showDeleteCategoryDialog(category: Category) {
        if (!category.key.isNullOrEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.cannot_delete_default_category), Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_category_title))
                .setMessage("Bạn chắc chắn muốn xoá danh mục '${category.name}'?\n\nCác giao dịch thuộc danh mục này sẽ được chuyển sang mục 'Khác'.")
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    // Bước 1: Chuyển các giao dịch cũ sang danh mục "Khác" để bảo toàn dữ liệu
                    val otherCategoryName = getString(R.string.cat_other)
                    dbHelper.migrateTransactions(category.name, otherCategoryName)
                    
                    // Bước 2: Xóa danh mục
                    dbHelper.deleteCategory(category.name)
                    loadAndSyncCategories()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    /**
     * Cập nhật lại dữ liệu khi Fragment quay lại trạng thái hoạt động.
     */
    override fun onResume() {
        super.onResume()
        loadAndSyncCategories()
    }
}
