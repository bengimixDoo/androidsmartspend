package com.example.smartspend2

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.smartspend2.models.Category
import com.example.smartspend2.models.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Activity chính của ứng dụng SmartSpend.
 *
 * Activity này đóng vai trò là container chứa các Fragment điều hướng chính (Home, Transactions, Categories, Reports).
 * Nó chịu trách nhiệm thiết lập thanh điều hướng dưới đáy (BottomNavigationView) và khởi tạo dữ liệu ban đầu.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Được gọi khi Activity được khởi tạo.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Thiết lập NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // Lấy NavController để quản lý điều hướng
        val navController = navHostFragment.navController

        // Thiết lập BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Khởi tạo Database và dữ liệu mặc định
        val dbHelper = DatabaseHelper(this)
        syncDefaultCategoryNames(this, dbHelper)
        createDefaultCategoriesIfNeeded(this, dbHelper)
        addDummyData(dbHelper)

        // Liên kết BottomNavigationView với NavController để tự động xử lý điều hướng
        bottomNav.setupWithNavController(navController)
    }
}

/**
 * Data class lưu trữ thông tin cấu hình cho các danh mục mặc định.
 *
 * @property key Khóa định danh duy nhất cho danh mục (dùng để đồng bộ đa ngôn ngữ).
 * @property nameResId ID tài nguyên chuỗi (string resource) cho tên danh mục.
 * @property isExpense Xác định danh mục này thuộc loại Chi tiêu (true) hay Thu nhập (false).
 */
private data class DefaultCategoryInfo(val key: String, val nameResId: Int, val isExpense: Boolean)

/**
 * Danh sách định nghĩa các danh mục mặc định của ứng dụng.
 */
private val defaultCategoryDefinitions = listOf(
    DefaultCategoryInfo("cat_food", R.string.cat_food, true),
    DefaultCategoryInfo("cat_transport", R.string.cat_transport, true),
    DefaultCategoryInfo("cat_bills", R.string.cat_bills, true),
    DefaultCategoryInfo("cat_entertainment", R.string.cat_entertainment, true),
    DefaultCategoryInfo("cat_shopping", R.string.cat_shopping, true),
    DefaultCategoryInfo("cat_health", R.string.cat_health, true),
    DefaultCategoryInfo("cat_education", R.string.cat_education, true),
    DefaultCategoryInfo("cat_salary", R.string.cat_salary, false),
    DefaultCategoryInfo("cat_bonus", R.string.cat_bonus, false),
    DefaultCategoryInfo("cat_allowance", R.string.cat_allowance, false),
    DefaultCategoryInfo("cat_investment", R.string.cat_investment, false),
    DefaultCategoryInfo("cat_selling", R.string.cat_selling, false),
    DefaultCategoryInfo("cat_gifted", R.string.cat_gifted, false),
    DefaultCategoryInfo("cat_other", R.string.cat_other, false) // "Khác" có thể dùng cho cả thu và chi
)

/**
 * Đồng bộ tên của các danh mục mặc định trong cơ sở dữ liệu với ngôn ngữ hiện tại của thiết bị.
 *
 * Hàm này kiểm tra các danh mục có `key` (danh mục mặc định) và cập nhật tên hiển thị
 * nếu ngôn ngữ hệ thống đã thay đổi so với lần lưu trước đó.
 *
 * @param context Context ứng dụng để truy cập tài nguyên.
 * @param dbHelper Helper để thao tác với cơ sở dữ liệu.
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
 * Tạo các danh mục mặc định trong cơ sở dữ liệu nếu chúng chưa tồn tại.
 *
 * Hàm này thường chỉ chạy trong lần khởi chạy đầu tiên của ứng dụng.
 *
 * @param context Context ứng dụng.
 * @param dbHelper Helper để thao tác với cơ sở dữ liệu.
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

/**
 * Thêm dữ liệu giả lập (Dummy Data) vào cơ sở dữ liệu phục vụ mục đích kiểm thử/phát triển.
 *
 * Chỉ thêm dữ liệu nếu bảng giao dịch hiện tại đang trống.
 *
 * @param dbHelper Helper để thao tác với cơ sở dữ liệu.
 */
fun addDummyData(dbHelper: DatabaseHelper) {
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
