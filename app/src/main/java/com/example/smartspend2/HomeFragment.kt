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
import androidx.core.graphics.toColorInt

/**
 * Fragment màn hình chính (Dashboard) của ứng dụng.
 *
 * Chịu trách nhiệm hiển thị:
 * - Biểu đồ tròn (PieChart) tổng quan chi tiêu theo danh mục.
 * - Danh sách chú thích (Legend) tương ứng với biểu đồ.
 * - Danh sách 5 giao dịch gần nhất.
 */
class HomeFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var rvTransactions: RecyclerView
    private lateinit var rvLegend: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private var transactions: MutableList<Transaction> = mutableListOf()

    /**
     * Khởi tạo View cho Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Được gọi ngay sau khi View đã được khởi tạo.
     * Thực hiện ánh xạ View, cấu hình RecyclerView và tải dữ liệu ban đầu.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        pieChart = view.findViewById(R.id.pieChart)
        rvTransactions = view.findViewById(R.id.rvTransactions)
        rvLegend = view.findViewById(R.id.rvLegend)

        rvLegend.layoutManager = GridLayoutManager(requireContext(), 2)
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        loadDataAndUpdateUI()
    }

    /**
     * Tải dữ liệu từ Database và cập nhật toàn bộ UI.
     *
     * Quy trình:
     * 1. Lấy toàn bộ giao dịch, sắp xếp theo thời gian và hiển thị 5 giao dịch gần nhất.
     * 2. Tính toán và vẽ biểu đồ tròn dựa trên dữ liệu chi tiêu.
     */
    private fun loadDataAndUpdateUI() {
        val allData = dbHelper.getAllTransactions()
        transactions.clear()
        transactions.addAll(allData)

        val recentTransactions = transactions.sortedByDescending { transaction ->
            parseDate(transaction.date)
        }.take(5)

        rvTransactions.adapter = TransactionAdapter(recentTransactions) { _, _ -> }
        setupPieChartAndLegend()
    }

    /**
     * Chuyển đổi chuỗi ngày tháng sang đối tượng Date để sắp xếp.
     *
     * @param dateString Chuỗi ngày dạng "dd MMM yyyy" (Locale.ENGLISH).
     * @return Đối tượng Date, hoặc Date(0) nếu lỗi định dạng (để xếp xuống cuối).
     */
    private fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(dateString)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Lỗi không thể phân tích ngày: '$dateString'", e)
            Date(0)
        }
    }

    /**
     * Cấu hình và hiển thị biểu đồ tròn (PieChart).
     *
     * - Lọc giao dịch chi tiêu.
     * - Nhóm theo danh mục và tính tổng.
     * - Cập nhật dữ liệu cho PieChart và LegendAdapter.
     */
    private fun setupPieChartAndLegend() {
        val expenseTransactions = transactions.filter { it.isExpense }

        if (expenseTransactions.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("Chưa có giao dịch chi tiêu nào")
            pieChart.invalidate()
            rvLegend.adapter = LegendAdapter(emptyList())
            return
        }

        val categoryTotals = expenseTransactions
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "")

        // Bảng màu tương phản cao cho các lát cắt
        val colors = listOf(
            "#E6194B".toColorInt(), // Đỏ
            "#3CB44B".toColorInt(), // Xanh lá
            "#FFE119".toColorInt(), // Vàng
            "#4363D8".toColorInt(), // Xanh dương
            "#F58231".toColorInt(), // Cam
            "#911EB4".toColorInt(), // Tím
            "#42D4F4".toColorInt()  // Xanh lơ
        )
        dataSet.colors = colors
        dataSet.sliceSpace = 2f
        dataSet.selectionShift = 5f
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)

        pieChart.apply {
            this.data = data
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)

            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            transparentCircleRadius = 55f
            holeRadius = 50f
            centerText = "Chi tiêu"
            setCenterTextSize(18f)
            setCenterTextColor("#202B3C".toColorInt())

            invalidate()
        }

        val legendItems = entries.mapIndexed { index, pieEntry ->
            // Lặp lại màu nếu số lượng danh mục vượt quá số lượng màu
            val color = colors[index % colors.size]
            LegendItem(color, pieEntry.label)
        }
        rvLegend.adapter = LegendAdapter(legendItems)
    }

    /**
     * Cập nhật lại dữ liệu khi người dùng quay lại màn hình này.
     */
    override fun onResume() {
        super.onResume()
        loadDataAndUpdateUI()
    }
}
