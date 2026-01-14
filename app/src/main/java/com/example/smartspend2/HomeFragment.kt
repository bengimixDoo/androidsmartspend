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
 * Fragment chính của ứng dụng, hiển thị tổng quan tình hình tài chính.
 *
 * Bao gồm:
 * - Một biểu đồ tròn (PieChart) thể hiện tỷ lệ chi tiêu theo từng danh mục.
 * - Một danh sách các chú thích (Legend) cho biểu đồ tròn.
 * - Một danh sách 5 giao dịch gần đây nhất.
 *
 * Dữ liệu sẽ được tự động làm mới mỗi khi người dùng quay lại màn hình này.
 */
class HomeFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var rvTransactions: RecyclerView
    private lateinit var rvLegend: RecyclerView
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

        pieChart = view.findViewById(R.id.pieChart)
        rvTransactions = view.findViewById(R.id.rvTransactions)
        rvLegend = view.findViewById(R.id.rvLegend)

        rvLegend.layoutManager = GridLayoutManager(requireContext(), 2)
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        loadDataAndUpdateUI()
    }

    /**
     * Tải tất cả dữ liệu từ cơ sở dữ liệu và cập nhật giao diện người dùng.
     *
     * Hàm này thực hiện các công việc sau:
     * 1. Lấy toàn bộ giao dịch từ `DatabaseHelper`.
     * 2. Sắp xếp các giao dịch theo ngày giảm dần và chỉ lấy 5 giao dịch gần nhất.
     * 3. Cập nhật `RecyclerView` hiển thị các giao dịch gần đây.
     * 4. Gọi hàm `setupPieChartAndLegend()` để vẽ lại biểu đồ và chú thích.
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
     * Phân tích một chuỗi ngày tháng thành đối tượng [Date] để phục vụ việc sắp xếp.
     *
     * Hàm này luôn sử dụng `Locale.ENGLISH` để phân tích vì dữ liệu ngày tháng
     * trong cơ sở dữ liệu đã được chuẩn hóa theo định dạng này ("dd MMM yyyy").
     *
     * @param dateString Chuỗi ngày tháng cần phân tích (ví dụ: "15 Jan 2026").
     * @return Một đối tượng [Date]. Nếu có lỗi, trả về một ngày trong quá khứ (epoch)
     * để đảm bảo các mục bị lỗi sẽ được xếp xuống cuối danh sách.
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
     * Thiết lập và vẽ biểu đồ tròn (PieChart) cùng với danh sách chú thích (Legend).
     *
     * Hàm này lọc ra các giao dịch chi tiêu, nhóm chúng theo danh mục, tính tổng chi,
     * sau đó cấu hình và hiển thị dữ liệu lên `PieChart` và `RecyclerView` chú thích.
     * Nếu không có dữ liệu chi tiêu, biểu đồ sẽ hiển thị thông báo.
     */
    private fun setupPieChartAndLegend() {
        val expenseTransactions = transactions.filter { it.isExpense }

        if (expenseTransactions.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("Chưa có giao dịch chi tiêu nào")
            pieChart.invalidate() // Cập nhật để hiển thị chữ "No data"
            rvLegend.adapter = LegendAdapter(emptyList()) // Xóa chú thích cũ
            return
        }

        val categoryTotals = expenseTransactions
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "")

        // Bảng màu có độ tương phản cao, dễ phân biệt
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
            // Lấy màu tương ứng từ danh sách, dùng toán tử `%` để lặp lại màu nếu hết
            val color = colors[index % colors.size]
            LegendItem(color, pieEntry.label)
        }
        rvLegend.adapter = LegendAdapter(legendItems)
    }

    /**
     * Được gọi khi Fragment trở nên hữu hình với người dùng.
     * Tải lại dữ liệu để đảm bảo giao diện luôn được cập nhật mới nhất.
     */
    override fun onResume() {
        super.onResume()
        loadDataAndUpdateUI()
    }
}
