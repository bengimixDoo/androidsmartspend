package com.example.smartspend2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.example.smartspend2.adapters.TransactionAdapter
import com.example.smartspend2.models.Transaction
import com.example.smartspend2.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    // Cho phép null để tránh crash nếu không tìm thấy view
    private var pieChart: PieChart? = null
    private var recyclerView: RecyclerView? = null

    private lateinit var dbHelper: DatabaseHelper
    private var transactions: MutableList<Transaction> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout cho Fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo Database
        dbHelper = DatabaseHelper(requireContext())

        // Ánh xạ View (Dùng ID chuẩn)
        pieChart = view.findViewById(R.id.pieChart)
        recyclerView = view.findViewById(R.id.rvTransactions)

        // Cấu hình RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // Load dữ liệu
        loadTransactions()
    }

    private fun loadTransactions() {
        transactions.clear()

        // Lấy dữ liệu từ DB
        val allData = dbHelper.getAllTransactions()
        transactions.addAll(allData)

        // SẮP XẾP AN TOÀN (Tránh crash nếu ngày tháng lỗi)
        val recentTransactions = transactions.sortedByDescending { transaction ->
            try {
                // Cố gắng đọc ngày tháng theo định dạng chuẩn
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(transaction.date) ?: Date()
            } catch (e: Exception) {
                // Nếu ngày lỗi, in log ra xem và dùng ngày hiện tại để thay thế (không crash app)
                Log.e("HomeFragment", "Lỗi ngày tháng: ${transaction.date}")
                Date()
            }
        }.take(5) // Chỉ lấy 5 cái mới nhất

        // Đổ dữ liệu vào Adapter
        recyclerView?.adapter = TransactionAdapter(recentTransactions) { _, _ -> }

        // Vẽ lại biểu đồ
        setupPieChart()
    }

    private fun setupPieChart() {
        // Kiểm tra an toàn: Nếu View chưa tìm thấy thì không vẽ gì cả
        if (pieChart == null) return

        if (transactions.isEmpty()) {
            pieChart?.clear()
            pieChart?.setNoDataText("Chưa có dữ liệu chi tiêu")
            return
        }

        // Chỉ lấy các khoản CHI (Expense) để vẽ
        val expenseTransactions = transactions.filter { it.isExpense }

        // Nhóm theo danh mục và tính tổng
        val categoryTotals = expenseTransactions
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        // Nếu không có khoản chi nào -> Xóa biểu đồ
        if (entries.isEmpty()) {
            pieChart?.clear()
            return
        }

        val dataSet = PieDataSet(entries, "")

        // Màu sắc biểu đồ
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.primary),
            ContextCompat.getColor(requireContext(), R.color.accent),
            Color.parseColor("#FFA726"),
            Color.parseColor("#66BB6A"),
            Color.parseColor("#EF5350")
        )
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.WHITE)

        // Cấu hình giao diện biểu đồ
        pieChart?.apply {
            this.data = data
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            transparentCircleRadius = 55f
            holeRadius = 50f
            setCenterText("Expenses")
            setCenterTextSize(16f)
            setCenterTextColor(Color.DKGRAY)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(14f)

            // Cấu hình chú thích (Legend)
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 14f
                form = Legend.LegendForm.CIRCLE
            }

            // Vẽ lên màn hình
            invalidate()
        }
    }
}