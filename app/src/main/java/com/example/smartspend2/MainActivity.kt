package com.example.smartspend2

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.smartspend2.models.Category
import com.example.smartspend2.models.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Tìm cái khung bản đồ (NavHostFragment) mà chúng ta vừa đặt tên mới trong XML
        // Lưu ý: Phải dùng đúng ID R.id.nav_host_fragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // 2. Lấy trình điều khiển từ nó
        val navController = navHostFragment.navController

        // 3. Tìm cái thanh Menu dưới đáy
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val dbHelper = DatabaseHelper(this)
        // 1. Đồng bộ tên các danh mục mặc định phòng trường hợp đổi ngôn ngữ
        syncDefaultCategoryNames(this, dbHelper)
        // 2. Tạo các danh mục mặc định nếu chúng chưa tồn tại
        createDefaultCategoriesIfNeeded(this, dbHelper)
        // Chỉ thêm dữ liệu giao dịch giả khi phát triển (nếu DB trống)
        addDummyData(dbHelper)

        // 4. Dùng lệnh "thần thánh" này để tự động nối Menu với Màn hình
        // (Nó sẽ tự biết bấm nút Home -> mở nav_home, bấm Reports -> mở nav_reports)
        bottomNav.setupWithNavController(navController)
    }
}

/**
 * Cấu trúc để định nghĩa một danh mục mặc định.
 * @param key Mã định danh không đổi, trùng với tên trong strings.xml (ví dụ: "cat_food").
 * @param nameResId ID của chuỗi trong strings.xml (ví dụ: R.string.cat_food).
 * @param isExpense Là danh mục chi tiêu hay thu nhập.
 */
private data class DefaultCategoryInfo(val key: String, val nameResId: Int, val isExpense: Boolean)

private val defaultCategoryDefinitions = listOf(
    DefaultCategoryInfo("cat_food", R.string.cat_food, true),
    DefaultCategoryInfo("cat_transport", R.string.cat_transport, true),
    DefaultCategoryInfo("cat_bills", R.string.cat_bills, true),
    DefaultCategoryInfo("cat_entertainment", R.string.cat_entertainment, true),
    DefaultCategoryInfo("cat_shopping", R.string.cat_shopping, true),
    DefaultCategoryInfo("cat_salary", R.string.cat_salary, false),
    DefaultCategoryInfo("cat_other", R.string.cat_other, false) // "Khác" có thể dùng cho cả thu và chi
)

/**
 * Đồng bộ tên của các danh mục mặc định trong DB với file strings.xml hiện tại.
 * Rất quan trọng khi người dùng thay đổi ngôn ngữ của thiết bị.
 */
private fun syncDefaultCategoryNames(context: Context, dbHelper: DatabaseHelper) {
    val defaultCategoriesFromDb = dbHelper.getAllCategories().filter { !it.key.isNullOrEmpty() }
    if (defaultCategoriesFromDb.isEmpty()) return // Chưa có gì để đồng bộ

    defaultCategoriesFromDb.forEach { categoryInDb ->
        // Lấy ID của resource string từ key đã lưu trong DB
        val resId = context.resources.getIdentifier(categoryInDb.key, "string", context.packageName)
        if (resId != 0) {
            val currentName = context.getString(resId)
            // Nếu tên trong DB khác với tên theo ngôn ngữ hiện tại -> Cập nhật lại
            if (categoryInDb.name != currentName) {
                val updatedCategory = categoryInDb.copy(name = currentName)
                dbHelper.updateCategory(updatedCategory)
            }
        }
    }
}

/**
 * Tạo các danh mục mặc định nếu chúng chưa tồn tại trong cơ sở dữ liệu.
 * Hàm này chỉ thực sự thêm dữ liệu trong lần chạy đầu tiên của ứng dụng.
 */
private fun createDefaultCategoriesIfNeeded(context: Context, dbHelper: DatabaseHelper) {
    // Kiểm tra xem đã có danh mục mặc định nào chưa bằng cách tìm key
    val hasDefaults = dbHelper.getAllCategories().any { !it.key.isNullOrEmpty() }
    if (hasDefaults) return

    defaultCategoryDefinitions.forEach { info ->
        val category = Category(
            name = context.getString(info.nameResId),
            allocatedAmount = 0f,
            spentAmount = 0f,
            isExpense = info.isExpense,
            key = info.key
        )
        dbHelper.insertCategory(category)
    }
}

// Hàm tạo dữ liệu giao dịch giả lập (Dummy Data) để test
fun addDummyData(dbHelper: DatabaseHelper) {
    // Nếu trong DB đã có giao dịch rồi thì thôi không thêm nữa (tránh trùng lặp)
    if (dbHelper.getAllTransactions().isNotEmpty()) return
    
    val dummyList = listOf(

        Transaction(0, "Phở bò", 45000f, "Ăn uống", "14 Jan 2026", true),
        Transaction(0, "Cà phê", 25000f, "Ăn uống", "14 Jan 2026", true),
        Transaction(0, "Đổ xăng", 60000f, "Di chuyển", "12 Jan 2026", true),
        Transaction(0, "Tiền nhà", 3000000f, "Hóa đơn", "01 Jan 2026", true),
        Transaction(0, "Lương tháng", 10000000f, "Lương", "05 Jan 2026", false)
    )

    // Chèn từng cái vào Database
    for (item in dummyList) {
        dbHelper.insertTransaction(item)
    }
}
