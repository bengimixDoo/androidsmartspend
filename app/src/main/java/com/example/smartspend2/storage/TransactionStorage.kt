package com.example.smartspend2.storage

import android.content.Context
import com.example.smartspend2.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionStorage {
    private const val PREF_NAME = "SmartSpendPrefs"
    private const val KEY_TRANSACTIONS = "transactions"

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(transactions)
        editor.putString(KEY_TRANSACTIONS, json)
        editor.apply()
    }

    fun loadTransactions(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun calculateCategorySpending(context: Context): Map<String, Float> {
        val transactions = loadTransactions(context)
        val spendingMap = mutableMapOf<String, Float>()

        transactions.forEach { transaction ->
            val spent = spendingMap[transaction.category] ?: 0f
            spendingMap[transaction.category] = spent + transaction.amount
        }
        return spendingMap
    }
}