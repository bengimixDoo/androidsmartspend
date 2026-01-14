package com.example.smartspend2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.smartspend2.models.Transaction
import com.example.smartspend2.models.Category

/**
 * Lớp quản lý cơ sở dữ liệu SQLite chính của ứng dụng.
 *
 * Cung cấp các phương thức để:
 * - Quản lý vòng đời Database (tạo, nâng cấp).
 * - Thao tác CRUD với bảng Giao dịch (Transactions) và Danh mục (Categories).
 * - Thực hiện các truy vấn thống kê báo cáo.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "smartspend.db"
        private const val DATABASE_VERSION = 4

        // --- Transaction Table Constants ---
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_TRANSACTION_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IS_EXPENSE = "is_expense"

        // --- Category Table Constants ---
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_CATEGORY_ID = "category_id"
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_KEY = "category_key" // ĐỔI TÊN: Tránh từ khóa "key" của SQL
        private const val COLUMN_ALLOCATED_AMOUNT = "allocated_amount"
        private const val COLUMN_SPENT_AMOUNT = "spent_amount"
        private const val COLUMN_IS_EXPENSE_CATEGORY = "is_expense_category" // ĐỔI TÊN: Rõ nghĩa hơn

        // --- SQL Creation Statements ---
        private const val CREATE_TRANSACTIONS_TABLE = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_AMOUNT REAL,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_IS_EXPENSE INTEGER
            )
        """

        private const val CREATE_CATEGORIES_TABLE = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT UNIQUE,
                $COLUMN_ALLOCATED_AMOUNT REAL,
                $COLUMN_SPENT_AMOUNT REAL,
                $COLUMN_IS_EXPENSE_CATEGORY INTEGER, 
                $COLUMN_CATEGORY_KEY TEXT
            )
        """
    }

    /**
     * Được gọi khi database được tạo lần đầu.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TRANSACTIONS_TABLE)
        db.execSQL(CREATE_CATEGORIES_TABLE)
    }

    /**
     * Xử lý nâng cấp database khi version thay đổi.
     * Thực hiện các thay đổi cấu trúc (ALTER TABLE) để bảo toàn dữ liệu cũ.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // V2: Thêm cột is_expense
            try { 
                db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN is_expense INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE $TABLE_CATEGORIES ADD COLUMN is_expense INTEGER NOT NULL DEFAULT 1")
            } catch (e: android.database.sqlite.SQLiteException) {
            }
        }

        if (oldVersion < 3) {
            // Nâng cấp từ v2 lên v3: Thêm cột `key` vào bảng categories
            try {
                db.execSQL("ALTER TABLE $TABLE_CATEGORIES ADD COLUMN $COLUMN_CATEGORY_KEY TEXT")
            } catch (e: android.database.sqlite.SQLiteException) {
                // Bỏ qua lỗi nếu cột đã tồn tại
            }
        }

        if (oldVersion < 4) {
            // Nâng cấp từ v3 lên v4: Đổi tên cột "is_expense" thành "is_expense_category" trong bảng categories
            try {
                db.execSQL("ALTER TABLE $TABLE_CATEGORIES RENAME COLUMN is_expense TO $COLUMN_IS_EXPENSE_CATEGORY")
            } catch (e: android.database.sqlite.SQLiteException) {
                // Bỏ qua lỗi nếu cột đã được đổi tên hoặc không tồn tại
            }
        }
    }


    // --- Transaction Operations ---

    /**
     * Lấy danh sách tất cả giao dịch, sắp xếp theo ngày giảm dần.
     */
    fun getAllTransactions(): List<Transaction> {
        val db = readableDatabase
        val transactions = mutableListOf<Transaction>()

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS ORDER BY $COLUMN_DATE DESC",
            null
        )

        while (cursor.moveToNext()) {
            val transaction = Transaction(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                amount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                isExpense = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_EXPENSE)) == 1
            )
            transactions.add(transaction)
        }

        cursor.close()
        return transactions
    }

    /**
     * Thêm một giao dịch mới vào database.
     */
    fun insertTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, transaction.title)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_IS_EXPENSE, if (transaction.isExpense) 1 else 0)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    /**
     * Xóa một giao dịch dựa trên ID.
     */
    fun deleteTransaction(id: Long) {
        writableDatabase.delete(TABLE_TRANSACTIONS, "$COLUMN_TRANSACTION_ID = ?", arrayOf(id.toString()))
    }

    /**
     * Cập nhật thông tin một giao dịch đã tồn tại.
     */
    fun updateTransaction(id: Long, transaction: Transaction) {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, transaction.title)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_IS_EXPENSE, if (transaction.isExpense) 1 else 0)
        }
        writableDatabase.update(TABLE_TRANSACTIONS, values, "$COLUMN_TRANSACTION_ID = ?", arrayOf(id.toString()))
    }

    /**
     * Chuyển tất cả giao dịch từ danh mục cũ sang danh mục mới.
     * Thường dùng trước khi xóa một danh mục để bảo toàn dữ liệu.
     *
     * @param oldCategory Tên danh mục cũ.
     * @param newCategory Tên danh mục mới (thường là "Khác").
     */
    fun migrateTransactions(oldCategory: String, newCategory: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, newCategory)
        }
        db.update(TABLE_TRANSACTIONS, values, "$COLUMN_CATEGORY = ?", arrayOf(oldCategory))
    }

    // --- Category Operations ---

    /**
     * Thêm một danh mục mới.
     */
    fun insertCategory(category: Category): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, category.name)
            put(COLUMN_ALLOCATED_AMOUNT, category.allocatedAmount)
            put(COLUMN_SPENT_AMOUNT, category.spentAmount)
            put(COLUMN_IS_EXPENSE_CATEGORY, if (category.isExpense) 1 else 0)
            put(COLUMN_CATEGORY_KEY, category.key)
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    /**
     * Lấy danh sách tất cả danh mục, sắp xếp theo tên A-Z.
     */
    fun getAllCategories(): List<Category> {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES ORDER BY $COLUMN_CATEGORY_NAME ASC", null)
        val categories = mutableListOf<Category>()

        while (cursor.moveToNext()) {
            val keyIndex = cursor.getColumnIndex(COLUMN_CATEGORY_KEY)
            val key = if (keyIndex != -1 && !cursor.isNull(keyIndex)) cursor.getString(keyIndex) else null
            val category = Category(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                allocatedAmount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ALLOCATED_AMOUNT)),
                spentAmount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SPENT_AMOUNT)),
                isExpense = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_EXPENSE_CATEGORY)) == 1,
                key = key
            )
            categories.add(category)
        }
        cursor.close()
        return categories
    }

    /**
     * Xóa một danh mục dựa trên tên.
     */
    fun deleteCategory(name: String) {
        val db = writableDatabase
        db.delete(TABLE_CATEGORIES, "$COLUMN_CATEGORY_NAME = ?", arrayOf(name))
    }

    /**
     * Tính tổng chi tiêu cho từng danh mục.
     * @return Map với Key là tên danh mục, Value là tổng tiền chi.
     */
    fun calculateCategorySpending(): Map<String, Float> {
        val db = readableDatabase
        val spendingMap = mutableMapOf<String, Float>()

        val cursor = db.rawQuery(
            "SELECT $COLUMN_CATEGORY, SUM($COLUMN_AMOUNT) " +
                    "as total_spent " +
                    "FROM $TABLE_TRANSACTIONS " +
                    "WHERE $COLUMN_IS_EXPENSE = 1 " +
                    "GROUP BY $COLUMN_CATEGORY",
            null
        )

        while (cursor.moveToNext()) {
            val categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
            val totalSpent = cursor.getFloat(cursor.getColumnIndexOrThrow("total_spent"))
            spendingMap[categoryName] = totalSpent
        }
        cursor.close()
        return spendingMap
    }

    /**
     * Tính tổng thu nhập cho từng danh mục.
     * @return Map với Key là tên danh mục, Value là tổng tiền thu.
     */
    fun calculateCategoryIncome(): Map<String, Float> {
        val map = mutableMapOf<String, Float>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_CATEGORY, SUM($COLUMN_AMOUNT) " +
                    "as total_spent " +
                    "FROM $TABLE_TRANSACTIONS " +
                    "WHERE $COLUMN_IS_EXPENSE = 0 " +
                    "GROUP BY $COLUMN_CATEGORY",
            null)
        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(0)
                val total = cursor.getFloat(1)
                map[category] = total
            } while (cursor.moveToNext())
        }
        cursor.close()
        return map
    }


    /**
     * Cập nhật thông tin danh mục (tên, ngân sách, số tiền đã chi...).
     */
    fun updateCategory(category: Category): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, category.name)
            put(COLUMN_ALLOCATED_AMOUNT, category.allocatedAmount)
            put(COLUMN_SPENT_AMOUNT, category.spentAmount)
            put(COLUMN_IS_EXPENSE_CATEGORY, if (category.isExpense) 1 else 0)
        } 

        return db.update(
            TABLE_CATEGORIES,
            values,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(category.id.toString())
        )
    }

    /**
     * Lấy danh sách danh mục theo loại (Thu hoặc Chi).
     */
    fun getCategoriesByType(isExpense: Boolean): List<Category> {
        val db = readableDatabase
        val categories = mutableListOf<Category>()
        val selection = "$COLUMN_IS_EXPENSE_CATEGORY = ?"
        val selectionArgs = arrayOf(if (isExpense) "1" else "0")
        val cursor = db.query(
            TABLE_CATEGORIES,
            null, // all columns
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_CATEGORY_NAME ASC"
        )

        while (cursor.moveToNext()) {
            val keyIndex = cursor.getColumnIndex(COLUMN_CATEGORY_KEY)
            val key = if (keyIndex != -1 && !cursor.isNull(keyIndex)) cursor.getString(keyIndex) else null
            val category = Category(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                allocatedAmount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ALLOCATED_AMOUNT)),
                spentAmount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SPENT_AMOUNT)),
                isExpense = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_EXPENSE_CATEGORY)) == 1,
                key = key
            )
            categories.add(category)
        }
        cursor.close()
        return categories
    }

}
