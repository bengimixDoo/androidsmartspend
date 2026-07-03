package com.example.smartspend2

import android.content.Context
import android.util.Log
import com.example.smartspend2.models.Category
import com.example.smartspend2.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CloudSyncManager {

    private const val TAG = "CloudSyncManager"
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    fun uploadTransaction(transaction: Transaction) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("users").document(uid).collection("transactions").document(transaction.id.toString())
        docRef.set(transaction)
            .addOnSuccessListener { Log.d(TAG, "Transaction uploaded: ${transaction.id}") }
            .addOnFailureListener { e -> Log.e(TAG, "Error uploading transaction", e) }
    }

    fun deleteTransaction(transactionId: Long) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("transactions").document(transactionId.toString()).delete()
    }

    fun uploadCategory(category: Category) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("users").document(uid).collection("categories").document(category.id.toString())
        docRef.set(category)
            .addOnSuccessListener { Log.d(TAG, "Category uploaded: ${category.id}") }
            .addOnFailureListener { e -> Log.e(TAG, "Error uploading category", e) }
    }

    fun deleteCategory(categoryName: String) {
        // Cần tìm category theo tên để xóa vì DatabaseHelper.deleteCategory dùng tên.
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("categories")
            .whereEqualTo("name", categoryName)
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    doc.reference.delete()
                }
            }
    }

    // Coroutine function để tải toàn bộ dữ liệu khi đăng nhập vào thiết bị mới
    suspend fun downloadAllDataToLocal(context: Context) {
        val uid = auth.currentUser?.uid ?: return
        val dbHelper = DatabaseHelper(context)
        
        try {
            // 1. Tải Profile (budget, card, cash)
            val profileDoc = firestore.collection("users").document(uid).collection("profile").document("info").get().await()
            if (profileDoc.exists()) {
                val budget = profileDoc.getLong("budget")?.toInt() ?: 0
                val card = profileDoc.getLong("card")?.toInt() ?: 0
                val cash = profileDoc.getLong("cash")?.toInt() ?: 0
                
                val prefs = context.getSharedPreferences("SmartSpendPrefs_$uid", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putInt("budget", budget)
                    putInt("card", card)
                    putInt("cash", cash)
                    putBoolean("first_time", false)
                }.apply()
            }

            // 2. Xóa DB local hiện tại trước khi sync để tránh trùng lặp nếu có
            dbHelper.clearDatabase()

            // 3. Tải Categories
            val categoriesSnapshot = firestore.collection("users").document(uid).collection("categories").get().await()
            for (doc in categoriesSnapshot.documents) {
                val cat = doc.toObject(Category::class.java)
                if (cat != null) {
                    dbHelper.insertCategorySync(cat)
                }
            }

            // 4. Tải Transactions
            val transactionsSnapshot = firestore.collection("users").document(uid).collection("transactions").get().await()
            for (doc in transactionsSnapshot.documents) {
                val trans = doc.toObject(Transaction::class.java)
                if (trans != null) {
                    dbHelper.insertTransactionSync(trans)
                }
            }

            Log.d(TAG, "Download all data complete!")

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading data", e)
        }
    }
}
