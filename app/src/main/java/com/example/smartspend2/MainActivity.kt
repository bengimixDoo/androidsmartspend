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
        addDummyData(dbHelper)

        // 4. Dùng lệnh "thần thánh" này để tự động nối Menu với Màn hình
        // (Nó sẽ tự biết bấm nút Home -> mở nav_home, bấm Reports -> mở nav_reports)
        bottomNav.setupWithNavController(navController)
    }
}
// Hàm tạo dữ liệu giả lập (Dummy Data)
fun addDummyData(dbHelper: DatabaseHelper) {
    // Nếu trong DB đã có dữ liệu rồi thì thôi không thêm nữa (tránh trùng lặp)
    if (dbHelper.getAllTransactions().isNotEmpty()) return

    val dummyList = listOf(
        // --- KHOẢN CHI (EXPENSE) ---
        // Food
        Transaction(0, "Phở bò ăn sáng", 45000f, "Food", "12 Jan 2026", true),
        Transaction(0, "Cà phê Highland", 29000f, "Food", "12 Jan 2026", true),
        Transaction(0, "Lẩu nướng cuối tuần", 350000f, "Food", "10 Jan 2026", true),
        Transaction(0, "Siêu thị mua rau", 120000f, "Food", "08 Jan 2026", true),

        // Transport
        Transaction(0, "Đổ xăng xe máy", 80000f, "Transport", "11 Jan 2026", true),
        Transaction(0, "Grab đi làm", 35000f, "Transport", "09 Jan 2026", true),
        Transaction(0, "Bảo dưỡng xe", 150000f, "Transport", "05 Jan 2026", true),

        // Bills
        Transaction(0, "Tiền điện tháng 12", 560000f, "Bills", "01 Jan 2026", true),
        Transaction(0, "Tiền nước", 80000f, "Bills", "02 Jan 2026", true),
        Transaction(0, "Internet FPT", 220000f, "Bills", "03 Jan 2026", true),
        Transaction(0, "Tiền thuê nhà", 3500000f, "Bills", "01 Jan 2026", true),

        // Entertainment
        Transaction(0, "Xem phim CGV", 180000f, "Entertainment", "07 Jan 2026", true),
        Transaction(0, "Mua game Steam", 250000f, "Entertainment", "06 Jan 2026", true),
        Transaction(0, "Gói Netflix", 260000f, "Entertainment", "04 Jan 2026", true),

        // Shopping
        Transaction(0, "Mua áo khoác", 450000f, "Shopping", "05 Jan 2026", true),
        Transaction(0, "Giày chạy bộ", 800000f, "Shopping", "02 Jan 2026", true),

        // --- KHOẢN THU (INCOME) ---
        Transaction(0, "Lương tháng 12", 15000000f, "Salary", "05 Jan 2026", false),
        Transaction(0, "Thưởng Tết Dương", 2000000f, "Salary", "01 Jan 2026", false),
        Transaction(0, "Bán đồ cũ", 500000f, "Other", "10 Jan 2026", false),
        Transaction(0, "Lì xì sớm", 200000f, "Other", "12 Jan 2026", false)
    )

    // Chèn từng cái vào Database
    for (item in dummyList) {
        dbHelper.insertTransaction(item)
    }
}
