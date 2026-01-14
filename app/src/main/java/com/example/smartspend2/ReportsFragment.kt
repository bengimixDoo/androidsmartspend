package com.example.smartspend2

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.example.smartspend2.adapters.CategoryReportAdapter
import com.example.smartspend2.adapters.CategoryReportItem
import com.example.smartspend2.models.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    // Các View cũ
    private lateinit var spinnerTime: Spinner
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvBalance: TextView
    private lateinit var barChart: BarChart
    private lateinit var rvCategoryReport: RecyclerView
    private lateinit var topSpendingChart: HorizontalBarChart

    // --- CÁC VIEW MỚI CHO LINE CHART ---
    private lateinit var spinnerCategoryTrend: Spinner
    private lateinit var lineChart: LineChart

    private val allTransactions = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        // 1. Ánh xạ View
        spinnerTime = view.findViewById(R.id.spinnerTimeFilter)
        tvIncome = view.findViewById(R.id.tvTotalIncome)
        tvExpense = view.findViewById(R.id.tvTotalExpense)
        tvBalance = view.findViewById(R.id.tvNetBalance)
        barChart = view.findViewById(R.id.barChartTrend)
        rvCategoryReport = view.findViewById(R.id.rvCategoryReport)
        topSpendingChart = view.findViewById(R.id.topSpendingChart)

        // Ánh xạ mới
        spinnerCategoryTrend = view.findViewById(R.id.spinnerCategoryTrend)
        lineChart = view.findViewById(R.id.lineChartCategory)

        // Cấu hình RecyclerView
        rvCategoryReport.layoutManager = LinearLayoutManager(requireContext())
        rvCategoryReport.isNestedScrollingEnabled = false

        // Load dữ liệu
        allTransactions.clear()
        allTransactions.addAll(dbHelper.getAllTransactions())

        // 2. Setup các thành phần
        setupTimeSpinner()

        // Mặc định chọn "Tháng này"
        filterDataByTime(0)

        // Setup 2 biểu đồ xu hướng
        setupTrendBarChart()         // Cột (Thu vs Chi)
        setupCategoryTrendSpinner()  // Đường (Chi tiết danh mục)
    }

    // --- SETUP SPINNER THỜI GIAN (TOP) ---
    private fun setupTimeSpinner() {
        val timeOptions = listOf("Tháng này", "Tháng trước", "Năm nay", "Tất cả")
        spinnerTime.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, timeOptions)
        spinnerTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterDataByTime(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // --- SETUP SPINNER DANH MỤC CHO LINE CHART (NEW) ---
    private fun setupCategoryTrendSpinner() {
        // Lấy danh sách danh mục có thật trong DB
        val categories = dbHelper.getAllCategories().map { it.name }.toMutableList()
        // Thêm tùy chọn mặc định nếu thích, hoặc lấy cái đầu tiên
        if (categories.isEmpty()) return

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategoryTrend.adapter = adapter

        spinnerCategoryTrend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                setupTrendLineChart(selectedCategory) // Vẽ lại biểu đồ khi chọn danh mục khác
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // --- VẼ BIỂU ĐỒ ĐƯỜNG (LINE CHART) ---
    private fun setupTrendLineChart(categoryName: String) {
        val calendar = Calendar.getInstance()
        val last6Months = mutableListOf<String>()
        val entries = mutableListOf<Entry>()

        // Lùi lại 5 tháng để lấy dữ liệu 6 tháng gần nhất
        calendar.add(Calendar.MONTH, -5)

        for (i in 0 until 6) {
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val monthLabel = "T${month + 1}"
            last6Months.add(monthLabel)

            // Lọc giao dịch của danh mục này trong tháng đó
            val totalSpent = allTransactions.filter {
                val date = parseDate(it.date)
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH) == month &&
                        cal.get(Calendar.YEAR) == year &&
                        it.category == categoryName &&
                        it.isExpense // Chỉ tính chi tiêu
            }.sumOf { it.amount.toDouble() }.toFloat()

            entries.add(Entry(i.toFloat(), totalSpent))
            calendar.add(Calendar.MONTH, 1)
        }

        val dataSet = LineDataSet(entries, categoryName)
        dataSet.color = Color.parseColor("#4ECDC4") // Màu xanh ngọc
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(Color.parseColor("#4ECDC4"))
        dataSet.valueTextSize = 10f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Làm đường cong mềm mại
        dataSet.setDrawFilled(true) // Tô màu dưới đường
        dataSet.fillColor = Color.parseColor("#4ECDC4")
        dataSet.fillAlpha = 50

        val data = LineData(dataSet)
        lineChart.data = data

        // Cấu hình trục X
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(last6Months)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        // Tinh chỉnh giao diện
        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.axisMinimum = 0f // Bắt đầu từ 0
        lineChart.animateY(1000) // Hiệu ứng chuyển động

        lineChart.invalidate() // Vẽ
    }

    // --- VẼ BIỂU ĐỒ CỘT (BAR CHART - THU/CHI) ---
    private fun setupTrendBarChart() {
        // (Giữ nguyên code phần trước)
        val calendar = Calendar.getInstance()
        val last6Months = mutableListOf<String>()
        val incomeEntries = mutableListOf<BarEntry>()
        val expenseEntries = mutableListOf<BarEntry>()

        calendar.add(Calendar.MONTH, -5)

        for (i in 0 until 6) {
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val monthLabel = "T${month + 1}"
            last6Months.add(monthLabel)

            val transactionsInMonth = allTransactions.filter {
                val date = parseDate(it.date)
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
            }

            val income = transactionsInMonth.filter { !it.isExpense }.sumOf { it.amount.toDouble() }.toFloat()
            val expense = transactionsInMonth.filter { it.isExpense }.sumOf { it.amount.toDouble() }.toFloat()

            incomeEntries.add(BarEntry(i.toFloat(), income))
            expenseEntries.add(BarEntry(i.toFloat(), expense))
            calendar.add(Calendar.MONTH, 1)
        }

        val setIncome = BarDataSet(incomeEntries, "Thu")
        setIncome.color = Color.parseColor("#4ECDC4")

        val setExpense = BarDataSet(expenseEntries, "Chi")
        setExpense.color = Color.parseColor("#FF6B6B")

        val data = BarData(setIncome, setExpense)
        data.barWidth = 0.3f
        barChart.data = data

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(last6Months)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 6f

        barChart.description.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.groupBars(0f, 0.3f, 0.05f)
        barChart.invalidate()
    }

    // --- CÁC HÀM HỖ TRỢ KHÁC (GIỮ NGUYÊN) ---
    private fun filterDataByTime(optionIndex: Int) {
        val calendar = Calendar.getInstance()
        val filteredList = when (optionIndex) {
            0 -> { // Tháng này
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                allTransactions.filter {
                    val date = parseDate(it.date)
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }
            }
            1 -> { // Tháng trước
                calendar.add(Calendar.MONTH, -1)
                val prevMonth = calendar.get(Calendar.MONTH)
                val prevYear = calendar.get(Calendar.YEAR)
                allTransactions.filter {
                    val date = parseDate(it.date)
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.get(Calendar.MONTH) == prevMonth && cal.get(Calendar.YEAR) == prevYear
                }
            }
            2 -> { // Năm nay
                val currentYear = calendar.get(Calendar.YEAR)
                allTransactions.filter {
                    val date = parseDate(it.date)
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.get(Calendar.YEAR) == currentYear
                }
            }
            else -> allTransactions
        }
        updateReportUI(filteredList)
    }

    private fun updateReportUI(transactions: List<Transaction>) {
        val formatter = DecimalFormat("#,###")
        val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount.toDouble() }
        val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount.toDouble() }
        val balance = totalIncome - totalExpense

        tvIncome.text = "${formatter.format(totalIncome)} đ"
        tvExpense.text = "${formatter.format(totalExpense)} đ"
        tvBalance.text = "${formatter.format(balance)} đ"
        tvBalance.setTextColor(if (balance >= 0) Color.parseColor("#4ECDC4") else Color.parseColor("#FF6B6B"))

        val expenseTransactions = transactions.filter { it.isExpense }
        setupTopSpendingChart(expenseTransactions)
        updateCategoryReportList(expenseTransactions)
    }

    private fun setupTopSpendingChart(expenseList: List<Transaction>) {
        if (expenseList.isEmpty()) {
            topSpendingChart.clear()
            topSpendingChart.setNoDataText("Không có dữ liệu chi tiêu")
            topSpendingChart.invalidate()
            return
        }

        // Lấy top 5 và đảo ngược lại để mục lớn nhất hiển thị ở trên cùng
        val topList = expenseList
            .groupBy { it.category }
            .map { (cat, list) -> cat to list.sumOf { it.amount.toDouble() }.toFloat() }
            .sortedByDescending { it.second }
            .take(5)
            .reversed()

        if (topList.isEmpty()) {
            topSpendingChart.clear()
            topSpendingChart.setNoDataText("Không có dữ liệu chi tiêu")
            topSpendingChart.invalidate()
            return
        }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        topList.forEachIndexed { index, (category, amount) ->
            entries.add(BarEntry(index.toFloat(), amount))
            labels.add(category)
        }

        val dataSet = BarDataSet(entries, "Top Chi tiêu")
        dataSet.color = Color.parseColor("#F58231") // Màu cam
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.parseColor("#202B3C")
        dataSet.valueFormatter = object : ValueFormatter() {
            private val format = DecimalFormat("#,###")
            override fun getFormattedValue(value: Float): String {
                if (value == 0f) return ""
                return format.format(value)
            }
        }

        val data = BarData(dataSet)
        data.barWidth = 0.6f
        topSpendingChart.data = data

        // Cấu hình trục X (trục dọc chứa tên danh mục)
        val xAxis = topSpendingChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Hiển thị label bên trái
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textSize = 12f

        // --- GIẢI PHÁP MỚI: Tự động điều chỉnh khoảng trống ở cuối thanh dài nhất ---
        // Cách này sẽ tính toán giá trị lớn nhất trong dữ liệu và tăng giới hạn của trục giá trị lên một chút (ví dụ 20%).
        // Điều này tạo ra một "vùng đệm" tỷ lệ thuận với dữ liệu, hoạt động tốt trên mọi màn hình và mọi bộ dữ liệu.
        val leftAxis = topSpendingChart.axisLeft
        leftAxis.axisMinimum = 0f
        val maxAmount = topList.maxOfOrNull { it.second } ?: 0f
        // Thêm 20% khoảng đệm vào giá trị lớn nhất.
        // Nếu không có dữ liệu, đặt một giá trị mặc định để tránh lỗi.
        leftAxis.axisMaximum = if (maxAmount > 0) maxAmount * 1.2f else 100f
        leftAxis.isEnabled = false // Ẩn trục giá trị đi cho gọn

        topSpendingChart.axisRight.isEnabled = false
        topSpendingChart.description.isEnabled = false
        topSpendingChart.legend.isEnabled = false
        topSpendingChart.setTouchEnabled(false)
        topSpendingChart.invalidate()
    }

    private fun updateCategoryReportList(expenseList: List<Transaction>) {
        val reportList = expenseList
            .groupBy { it.category }
            .map { (cat, list) ->
                CategoryReportItem(
                    categoryName = cat,
                    totalAmount = list.sumOf { it.amount.toDouble() }.toFloat(),
                    transactionCount = list.size
                )
            }
            .sortedByDescending { it.totalAmount }
        rvCategoryReport.adapter = CategoryReportAdapter(reportList) {item ->

            // Lọc lấy các giao dịch thuộc danh mục này (từ danh sách đã lọc theo thời gian expenseList)
            val detailTransactions = expenseList.filter { it.category == item.categoryName }

            // Gọi hàm hiện Dialog
            showCategoryDetailDialog(item.categoryName, detailTransactions) }
    }

    private fun parseDate(dateString: String): Date {
        var date = try { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(dateString) } catch (e: Exception) { null }
        if (date == null) {
            date = try { SimpleDateFormat("dd MMM yyyy", Locale("vi", "VN")).parse(dateString) } catch (e: Exception) { null }
        }
        return date ?: Date(0)
    }
    // --- HÀM MỚI: HIỂN THỊ CHI TIẾT DANH MỤC ---
    private fun showCategoryDetailDialog(categoryName: String, transactions: List<Transaction>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_detail, null)

        // Ánh xạ View trong Dialog
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDetailTitle)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tvDetailTotal)
        val btnClose = dialogView.findViewById<View>(R.id.btnCloseDetail)
        val rvDetails = dialogView.findViewById<RecyclerView>(R.id.rvDetailTransactions)

        // Set dữ liệu
        tvTitle.text = "Chi tiết: $categoryName"

        val total = transactions.sumOf { it.amount.toDouble() }
        val formatter = DecimalFormat("#,###")
        tvTotal.text = "Tổng cộng: ${formatter.format(total)} đ"

        // Setup RecyclerView (Tái sử dụng TransactionAdapter có sẵn)
        rvDetails.layoutManager = LinearLayoutManager(requireContext())
        rvDetails.adapter = com.example.smartspend2.adapters.TransactionAdapter(transactions) { _, _ ->
            // Không làm gì khi click vào item con trong bảng chi tiết này (hoặc code thêm nếu muốn sửa/xóa)
        }

        // Tạo và hiện Dialog
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Làm nền trong suốt để bo góc đẹp
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}