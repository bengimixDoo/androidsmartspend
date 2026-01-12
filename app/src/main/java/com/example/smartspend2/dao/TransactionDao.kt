package com.example.smartspend2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.smartspend2.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Insert a new transaction
   /* @Insert
    suspend fun insertTransaction(transaction: Transaction)

    // Update an existing transaction
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // Delete a transaction
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // Get all transactions, ordered by date
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): Flow<List<Transaction>>

    // Get only income or expense transactions
    @Query("SELECT * FROM transactions WHERE isExpense = :isExpense ORDER BY date DESC")
    suspend fun getTransactionsByType(isExpense: Boolean): Flow<List<Transaction>>

    // Get total income
    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = 0")
    suspend fun getTotalIncome():Flow<Float>

    // Get total expenses
    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = 1")
    suspend fun getTotalExpenses(): Flow<Float>

    // Get total balance (income - expenses)
    @Query("SELECT (SELECT SUM(amount) FROM transactions WHERE isExpense = 0) - (SELECT SUM(amount) FROM transactions WHERE isExpense = 1)")
    suspend fun getTotalBalance(): Flow<Float>*/
}



