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
        private const val DATABASE_VERSION = 1

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
        private const val COLUMN_ALLOCATED_AMOUNT = "allocated_amount"
        private const val COLUMN_SPENT_AMOUNT = "spent_amount"
        private const val COLUMN_IS_EXPENSE_CATEGORY = "is_expense"

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
                $COLUMN_IS_EXPENSE_CATEGORY INTEGER
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TRANSACTIONS_TABLE)
        db.execSQL(CREATE_CATEGORIES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
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
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    fun getAllCategories(): List<Category> {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES ORDER BY $COLUMN_CATEGORY_NAME ASC", null)
        val categories = mutableListOf<Category>()

        while (cursor.moveToNext()) {
            val category = Category(
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                allocatedAmount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ALLOCATED_AMOUNT)),
                spentAmount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SPENT_AMOUNT)),
                isExpense = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_EXPENSE_CATEGORY)) == 1
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
            "SELECT $COLUMN_CATEGORY, SUM($COLUMN_AMOUNT) as total_spent FROM $TABLE_TRANSACTIONS WHERE $COLUMN_IS_EXPENSE = 1 GROUP BY $COLUMN_CATEGORY",
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

    //update category
    fun updateCategory(category: Category): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, category.name)
            put(COLUMN_ALLOCATED_AMOUNT, category.allocatedAmount)
            put(COLUMN_SPENT_AMOUNT, category.spentAmount)
            put(COLUMN_IS_EXPENSE_CATEGORY, if (category.isExpense) 1 else 0)
        }

        // Update the category where the name matches
        return db.update(
            TABLE_CATEGORIES,
            values,
            "$COLUMN_CATEGORY_NAME = ?",
            arrayOf(category.name)
        )
    }

}
