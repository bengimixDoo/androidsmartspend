package com.example.smartspend2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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
        // Luôn tạo các danh mục mặc định nếu chúng chưa tồn tại
        createDefaultCategoriesIfNeeded(dbHelper)
        // Chỉ thêm dữ liệu giao dịch giả khi phát triển (nếu DB trống)
        addDummyData(dbHelper)

        // 4. Dùng lệnh "thần thánh" này để tự động nối Menu với Màn hình
        // (Nó sẽ tự biết bấm nút Home -> mở nav_home, bấm Reports -> mở nav_reports)
        bottomNav.setupWithNavController(navController)
    }
}

// Hàm này chỉ chạy MỘT LẦN để tạo các danh mục cơ bản cho người dùng mới
fun createDefaultCategoriesIfNeeded(dbHelper: DatabaseHelper) {
    // Nếu trong DB đã có danh mục rồi thì thôi không thêm nữa (tránh trùng lặp)
    if (dbHelper.getAllCategories().isNotEmpty()) return

    // Thêm các danh mục mặc định
    val defaultCategories = listOf(
        // --- DANH MỤC CHI (EXPENSE) ---
        com.example.smartspend2.models.Category(name = "Food", allocatedAmount = 0f, spentAmount = 0f, isExpense = true),
        com.example.smartspend2.models.Category(name = "Transport", allocatedAmount = 0f, spentAmount = 0f, isExpense = true),
        com.example.smartspend2.models.Category(name = "Bills", allocatedAmount = 0f, spentAmount = 0f, isExpense = true),
        com.example.smartspend2.models.Category(name = "Entertainment", allocatedAmount = 0f, spentAmount = 0f, isExpense = true),
        com.example.smartspend2.models.Category(name = "Shopping", allocatedAmount = 0f, spentAmount = 0f, isExpense = true),
        // --- DANH MỤC THU (INCOME) ---
        com.example.smartspend2.models.Category(name = "Salary", allocatedAmount = 0f, spentAmount = 0f, isExpense = false),
        com.example.smartspend2.models.Category(name = "Other", allocatedAmount = 0f, spentAmount = 0f, isExpense = false)
    )
    for (category in defaultCategories) {
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
