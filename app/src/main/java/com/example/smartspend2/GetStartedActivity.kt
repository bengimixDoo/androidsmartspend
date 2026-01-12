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

        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val btnGetStarted: Button = findViewById(R.id.btnGetStarted)

        btnGetStarted.setOnClickListener {
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