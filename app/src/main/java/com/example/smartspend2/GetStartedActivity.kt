package com.example.smartspend2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_get_started)

        val btnGetStarted: Button = findViewById(R.id.btnGetStarted)

        btnGetStarted.setOnClickListener {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                val userId = currentUser.uid
                val dbFile = getDatabasePath("smartspend_$userId.db")
                
                if (!dbFile.exists()) {
                    // DB không tồn tại (app bị xoá data hoặc cài lại) nhưng user vẫn đang đăng nhập (do Android tự khôi phục token).
                    // Ta cần kiểm tra trên Firestore xem có dữ liệu không để tải về.
                    android.widget.Toast.makeText(this, "Đang đồng bộ dữ liệu từ Cloud...", android.widget.Toast.LENGTH_LONG).show()
                    btnGetStarted.isEnabled = false
                    
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("profile")
                        .document("info")
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                this@GetStartedActivity.lifecycleScope.launch {
                                    CloudSyncManager.downloadAllDataToLocal(this@GetStartedActivity)
                                    startActivity(Intent(this@GetStartedActivity, MainActivity::class.java))
                                    finish()
                                }
                            } else {
                                startActivity(Intent(this@GetStartedActivity, SetupActivity::class.java))
                                finish()
                            }
                        }
                        .addOnFailureListener {
                            startActivity(Intent(this@GetStartedActivity, SetupActivity::class.java))
                            finish()
                        }
                } else {
                    // DB đã tồn tại -> flow bình thường
                    val prefs = getSharedPreferences("SmartSpendPrefs_$userId", MODE_PRIVATE)
                    val firstTime = prefs.getBoolean("first_time", true)
                    if (firstTime) {
                        startActivity(Intent(this, SetupActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
            }
        }
    }
}