// Dán toàn bộ nội dung này vào file D:/BT/Android_Studio/androidsmartspend/app/src/main/java/com/example/smartspend2/HomeFragment.kt
package com.example.smartspend2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.adapters.LegendAdapter
import com.example.smartspend2.adapters.LegendItem
import com.example.smartspend2.adapters.TransactionAdapter
import com.example.smartspend2.models.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var rvTransactions: RecyclerView
    private lateinit var rvLegend: RecyclerView // RecyclerView mới cho chú thích

    private lateinit var dbHelper: DatabaseHelper
    private var transactions: MutableList<Transaction> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        // Ánh xạ các View
        pieChart = view.findViewById(R.id.pieChart)
        rvTransactions = view.findViewById(R.id.rvTransactions)
        rvLegend = view.findViewById(R.id.rvLegend) // Ánh xạ RecyclerView chú thích

        // Cấu hình RecyclerView cho chú thích, hiển thị 2 cột
        rvLegend.layoutManager = GridLayoutManager(requireContext(), 2)

        // Cấu hình RecyclerView cho danh sách giao dịch
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        loadDataAndUpdateUI()
    }

    private fun loadDataAndUpdateUI() {
        val allData = dbHelper.getAllTransactions()
        transactions.clear()
        transactions.addAll(allData)

        // SẮP XẾP THÔNG MINH (Hỗ trợ cả Tiếng Anh "Jan" và Tiếng Việt "Th1")
        val recentTransactions = transactions.sortedByDescending { transaction ->
            parseDateFlexible(transaction.date)
        }.take(5)

        rvTransactions.adapter = TransactionAdapter(recentTransactions) { _, _ -> }
        setupPieChartAndLegend()
    }

    // Hàm phụ trợ: Thử đọc mọi kiểu ngày tháng
    private fun parseDateFlexible(dateString: String): Date {
        // 1. Thử đọc theo Tiếng Anh (cho dữ liệu mẫu: Jan, Feb...)
        var date = try {
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(dateString)
        } catch (e: Exception) { null }

        // 2. Nếu thất bại, thử đọc theo Tiếng Việt (cho dữ liệu bạn nhập: Th1, Th2...)
        if (date == null) {
            date = try {
                SimpleDateFormat("dd MMM yyyy", Locale("vi", "VN")).parse(dateString)
            } catch (e: Exception) { null }
        }

        // 3. Nếu vẫn không được, thử theo ngôn ngữ mặc định của máy
        if (date == null) {
            date = try {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(dateString)
            } catch (e: Exception) { null }
        }

        // Nếu tất cả đều thua, trả về mốc thời gian gốc (1970)
        return date ?: Date(0)
    }

    private fun setupPieChartAndLegend() {
        // Lấy các giao dịch CHI để vẽ biểu đồ
        val expenseTransactions = transactions.filter { it.isExpense }

        if (expenseTransactions.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("Chưa có giao dịch chi tiêu nào")
            pieChart.invalidate() // Cập nhật để hiển thị chữ "No data"
            rvLegend.adapter = LegendAdapter(emptyList()) // Xóa chú thích cũ
            return
        }

        // Nhóm theo danh mục và tính tổng
        val categoryTotals = expenseTransactions
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "")

        // Danh sách màu sắc phong phú
        val colors = listOf(
            Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"), Color.parseColor("#FFD166"),
            Color.parseColor("#118AB2"), Color.parseColor("#06D6A0"), Color.parseColor("#7E57C2"),
            Color.parseColor("#EF5350")
        )
        dataSet.colors = colors
        dataSet.sliceSpace = 2f
        dataSet.selectionShift = 5f

        // ** Tắt các chữ vẽ trên biểu đồ cho sạch sẽ **
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)

        // --- Cấu hình PieChart ---
        pieChart.apply {
            this.data = data
            setUsePercentValues(true)
            description.isEnabled = false

            // ** TẮT CHÚ THÍCH MẶC ĐỊNH **
            legend.isEnabled = false

            // ** Tắt tên các mục vẽ trên biểu đồ **
            setDrawEntryLabels(false)

            // Cấu hình "lỗ" ở giữa
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            transparentCircleRadius = 55f
            holeRadius = 50f
            setCenterText("Chi tiêu")
            setCenterTextSize(18f)
            setCenterTextColor(Color.parseColor("#202B3C"))

            // Vẽ lại biểu đồ
            invalidate()
        }

        // --- CẬP NHẬT RecyclerView CHÚ THÍCH (Legend) ---
        val legendItems = entries.mapIndexed { index, pieEntry ->
            // Lấy màu tương ứng từ list `colors`, dùng toán tử `%` để lặp lại màu nếu hết
            val color = colors[index % colors.size]
            LegendItem(color, pieEntry.label)
        }
        rvLegend.adapter = LegendAdapter(legendItems)
    }

    override fun onResume() {
        super.onResume()
        // Tự động cập nhật dữ liệu mỗi khi quay lại màn hình này
        loadDataAndUpdateUI()
    }
}
