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

        // 4. Xử lý nút Thêm
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

    private fun showAddTransactionDialog(existingTransaction: Transaction? = null, position: Int? = null) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rbExpense)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        // --- SỬA: Lấy tên danh mục từ strings.xml để chuẩn tiếng Việt ---
        val defaultCategories = listOf(
            getString(R.string.cat_food),
            getString(R.string.cat_transport),
            getString(R.string.cat_bills),
            getString(R.string.cat_entertainment),
            getString(R.string.cat_shopping),
            getString(R.string.cat_salary),
            getString(R.string.cat_other)
        )

        // Load user-created categories from the database
        val userCategories = dbHelper.getAllCategories().map { it.name }

        // Combine and remove duplicates
        val categories = (defaultCategories + userCategories).distinct()

        // Set to spinner
        spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Populate fields if editing an existing transaction
        existingTransaction?.let {
            etTitle.setText(it.title)
            etAmount.setText(it.amount.toString())
            val categoryIndex = categories.indexOf(it.category)
            if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex)
            etDate.setText(it.date)
            if (it.isExpense) rbExpense.isChecked = true else rbIncome.isChecked = true
        }

        etDate.setOnClickListener {
            showDatePicker { date -> etDate.setText(date) }
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val amount = etAmount.text.toString().toFloatOrNull()
            val category = spinnerCategory.selectedItem.toString()
            val date = etDate.text.toString().trim()
            val isExpense = rbExpense.isChecked

            if (title.isEmpty() || amount == null || date.isEmpty()) {
                Toast.makeText(context, getString(R.string.msg_fill_error), Toast.LENGTH_SHORT).show()
            } else {
                val transaction = Transaction(
                    title = title,
                    amount = amount,
                    category = category,
                    date = date,
                    isExpense = isExpense
                )

                if (existingTransaction != null && position != null) {
                    dbHelper.updateTransaction(existingTransaction.id, transaction)
                    transactions[position] = transaction
                } else {
                    dbHelper.insertTransaction(transaction)
                    transactions.add(transaction)
                }

                transactionAdapter.notifyDataSetChanged()
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
                // Dùng Locale tiếng Anh để giữ format chuẩn cho DB,
                // hiển thị tiếng Việt đã có hàm xử lý ở HomeFragment
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
                        transactions.removeAt(position)
                        transactionAdapter.notifyDataSetChanged()
                        updateCategorySpending()
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