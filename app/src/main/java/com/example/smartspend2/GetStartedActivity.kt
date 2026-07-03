package com.example.smartspend2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

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
            } else {
                val userId = currentUser.uid
                val prefs = getSharedPreferences("SmartSpendPrefs_$userId", MODE_PRIVATE)
                val firstTime = prefs.getBoolean("first_time", true)
                if (firstTime) {
                    startActivity(Intent(this, SetupActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            finish()
        }
    }
}