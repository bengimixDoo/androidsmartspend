package com.example.smartspend2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.smartspend2.models.Transaction
import com.example.smartspend2.models.Category

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "smartspend.db"
        private const val DATABASE_VERSION = 4 // Nâng cấp phiên bản DB lên 4

        // Transaction Table
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_TRANSACTION_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IS_EXPENSE = "is_expense"

        // Category Table
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_CATEGORY_ID = "category_id"
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_KEY = "category_key" // ĐỔI TÊN: Tránh từ khóa "key" của SQL
        private const val COLUMN_ALLOCATED_AMOUNT = "allocated_amount"
        private const val COLUMN_SPENT_AMOUNT = "spent_amount"
        private const val COLUMN_IS_EXPENSE_CATEGORY = "is_expense_category" // ĐỔI TÊN: Rõ nghĩa hơn

        // Create Table Queries
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

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TRANSACTIONS_TABLE)
        db.execSQL(CREATE_CATEGORIES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Luôn kiểm tra phiên bản cũ để thực hiện nâng cấp đúng cách.
        // oldVersion là phiên bản database hiện tại trên máy người dùng.
        // newVersion là phiên bản mới bạn đã đặt trong code (DATABASE_VERSION = 2).

        if (oldVersion < 2) {
            // Nếu phiên bản cũ là 1, chúng ta cần thực hiện các thay đổi của phiên bản 2.
            // Cụ thể là thêm cột is_expense vào cả hai bảng. Dùng tên cột cũ để đảm bảo di chuyển đúng.
            try { 
                db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN is_expense INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE $TABLE_CATEGORIES ADD COLUMN is_expense INTEGER NOT NULL DEFAULT 1")
            } catch (e: android.database.sqlite.SQLiteException) {
                // Ghi lại log lỗi nếu cần, nhưng không làm crash app.
                // Lỗi có thể xảy ra nếu người dùng tự can thiệp DB,
                // hoặc trong một số kịch bản nâng cấp phức tạp.
                // Bỏ qua lỗi trong trường hợp này để app tiếp tục chạy.
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


    // transaction methods

    fun getAllTransactions(): List<Transaction> {
        val db = readableDatabase
        val transactions = mutableListOf<Transaction>()

        // Query to select all transactions
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

    // insert
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

    fun deleteTransaction(id: Long) {
        writableDatabase.delete(TABLE_TRANSACTIONS, "$COLUMN_TRANSACTION_ID = ?", arrayOf(id.toString()))
    }

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

    // Category Methods
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

    fun deleteCategory(name: String) {
        val db = writableDatabase
        db.delete(TABLE_CATEGORIES, "$COLUMN_CATEGORY_NAME = ?", arrayOf(name))
    }

    // Calculate total spending per category
    fun calculateCategorySpending(): Map<String, Float> {
        val db = readableDatabase
        val spendingMap = mutableMapOf<String, Float>()

        // Query to sum the spending by category
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

    // Hàm này tính tổng thu nhập cho mỗi category
    fun calculateCategoryIncome(): Map<String, Float> {
        val map = mutableMapOf<String, Float>()
        val db = this.readableDatabase
        // Thay đổi điều kiện WHERE isExpense = 0 để chỉ lấy các khoản THU
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


    //update category
    fun updateCategory(category: Category): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, category.name)
            put(COLUMN_ALLOCATED_AMOUNT, category.allocatedAmount)
            put(COLUMN_SPENT_AMOUNT, category.spentAmount)
            put(COLUMN_IS_EXPENSE_CATEGORY, if (category.isExpense) 1 else 0)
        } // Không cập nhật key vì nó là định danh cố định

        // Cập nhật danh mục dựa trên ID
        return db.update(
            TABLE_CATEGORIES,
            values,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(category.id.toString())
        )
    }

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
