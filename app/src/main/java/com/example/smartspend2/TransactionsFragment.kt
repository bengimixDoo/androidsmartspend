package com.example.smartspend2

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspend2.models.Category
import com.example.smartspend2.models.Transaction
import com.example.smartspend2.adapters.TransactionAdapter
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Khởi tạo Database và View
        dbHelper = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.rvTransactions)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 2. KHỞI TẠO ADAPTER TRƯỚC
        transactionAdapter = TransactionAdapter(transactions) { transaction, position ->
            showTransactionOptions(transaction, position)
        }
        recyclerView.adapter = transactionAdapter

        // 3. LOAD DỮ LIỆU SAU
        loadTransactions()

        // 4. Các nút bấm khác
        val fabAdd: View = view.findViewById(R.id.fabAddTransaction)
        fabAdd.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun loadTransactions() {
        transactions.clear()
        transactions.addAll(dbHelper.getAllTransactions())
        transactionAdapter.notifyDataSetChanged()
    }

    /**
     * Hàm hiển thị dialog để thêm/sửa giao dịch
     *
     */
    private fun showAddTransactionDialog(existingTransaction: Transaction? = null, position: Int? = null) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rbExpense)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Hàm helper để cập nhật spinner category dựa theo loại thu/chi
        fun updateCategorySpinner(isExpense: Boolean) {
            val categories = dbHelper.getCategoriesByType(isExpense).map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
            spinnerCategory.adapter = adapter

            // Nếu đang edit, chọn lại category cũ nếu nó tồn tại trong danh sách mới
            existingTransaction?.let {
                if (it.isExpense == isExpense) {
                    val categoryIndex = categories.indexOf(it.category)
                    if (categoryIndex >= 0) {
                        spinnerCategory.setSelection(categoryIndex)
                    }
                }
            }
        }

        // Thêm listener cho RadioButton để tự động cập nhật danh sách category
        rbExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateCategorySpinner(true)
        }
        rbIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateCategorySpinner(false)
        }

        // Populate fields if editing an existing transaction
        existingTransaction?.let {
            etTitle.setText(it.title)
            etAmount.setText(it.amount.toString())
            etDate.setText(it.date)
            // Set radio button, việc này sẽ tự động trigger listener ở trên để load đúng category
            if (it.isExpense) rbExpense.isChecked = true else rbIncome.isChecked = true
        } ?: run {
            // Nếu là giao dịch mới, mặc định là chi tiêu và load category chi tiêu
            rbExpense.isChecked = true
            updateCategorySpinner(true)
        }

        etDate.setOnClickListener {
            showDatePicker { date -> etDate.setText(date) }
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val amount = etAmount.text.toString().toFloatOrNull()
            val category = spinnerCategory.selectedItem?.toString() // An toàn hơn, tránh crash nếu spinner rỗng
            val date = etDate.text.toString().trim()
            val isExpense = rbExpense.isChecked

            if (title.isEmpty() || amount == null || date.isEmpty() || category == null) {
                Toast.makeText(context, "Vui lòng điền đủ thông tin và chọn danh mục.", Toast.LENGTH_LONG).show()
            } else {
                if (existingTransaction != null && position != null) {
                    val updatedTransaction = Transaction(existingTransaction.id, title, amount, category!!, date, isExpense)
                    dbHelper.updateTransaction(existingTransaction.id, updatedTransaction)
                } else {
                    val newTransaction = Transaction(0, title, amount, category!!, date, isExpense)
                    dbHelper.insertTransaction(newTransaction)
                }

                loadTransactions() // Load lại toàn bộ dữ liệu từ DB để đảm bảo tính nhất quán
                updateCategorySpending()
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
              val selectedDate = Calendar.getInstance()
              selectedDate.set(year, month, dayOfMonth)
              val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
              onDateSelected(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTransactionOptions(transaction: Transaction, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Tùy chọn giao dịch") // Tiếng Việt
            .setItems(arrayOf("Sửa", "Xóa")) { _, which -> // Tiếng Việt
                when (which) {
                    0 -> showAddTransactionDialog(transaction, position)
                    1 -> {
                        dbHelper.deleteTransaction(transaction.id)
                        loadTransactions() // Load lại list từ DB để đảm bảo đồng bộ
                        updateCategorySpending() // Cập nhật lại tiền đã tiêu cho category
                    }
                }
            }
            .show()
    }

    private fun updateCategorySpending() {
        val spendingMap = dbHelper.calculateCategorySpending()
        val categories = dbHelper.getAllCategories()
        val categoriesToCheck = mutableListOf<Category>()

        categories.forEach { category ->
            category.spentAmount = spendingMap[category.name] ?: 0f
            dbHelper.updateCategory(category)
            categoriesToCheck.add(category)
        }

        // Kiểm tra giới hạn chi tiêu sau khi cập nhật
        checkSpendingLimits(categoriesToCheck)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SPENDING_ALERTS",
                "Cảnh báo chi tiêu", // Tên kênh tiếng Việt
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi bạn vượt quá ngân sách" // Mô tả tiếng Việt
            }
            val manager = requireContext().getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun checkSpendingLimits(categoryList: List<Category>) {
        categoryList.forEach { category ->
            // Tránh chia cho 0
            if (category.allocatedAmount > 0) {
                val percentSpent = (category.spentAmount / category.allocatedAmount) * 100

                when {
                    percentSpent >= 100 -> {
                        sendNotification(
                            category.name,
                            "Cảnh báo! Ngân sách ${category.name} bị vượt quá!", "Hãy kiểm soát lại mức chi tiêu!"
                        )
                    }
                    percentSpent >= 90 -> {
                        sendNotification(
                            category.name,
                            "Cảnh báo! Sắp hết ngân sách ${category.name}",
                            "Bạn đã dùng ${percentSpent.toInt()}% ngân sách ${category.name}. Hãy chi tiêu chậm lại!"
                        )
                    }
                    percentSpent >= 80 -> {
                        sendNotification(
                            category.name,
                            "Chú ý! Ngân sách ${category.name} sắp đầy",
                            "Bạn đã dùng ${percentSpent.toInt()}% ngân sách ${category.name}. Hãy lên kế hoạch hợp lý!"
                        )
                    }
                }
            }
        }
    }

    private fun sendNotification(category: String, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = NotificationCompat.Builder(requireContext(), "SPENDING_ALERTS")
            .setSmallIcon(R.drawable.ic_notification) // Đảm bảo bạn có icon này
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(requireContext())
        // Sử dụng ID ngẫu nhiên hoặc hashcode để không bị đè thông báo
        manager.notify(category.hashCode(), builder.build())
    }


}
