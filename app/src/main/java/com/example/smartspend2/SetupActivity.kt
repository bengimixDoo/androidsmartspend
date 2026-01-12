package com.example.smartspend2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setup)

        val etBudget: EditText = findViewById(R.id.etBudget)
        val etCard: EditText = findViewById(R.id.etCard)
        val etCash: EditText = findViewById(R.id.etCash)
        val btnContinue: Button = findViewById(R.id.btnContinue)

        btnContinue.setOnClickListener {
            val budget = etBudget.text.toString().toIntOrNull()
            val card = etCard.text.toString().toIntOrNull()
            val cash = etCash.text.toString().toIntOrNull()

            if (budget == null || card == null || cash == null) {
                Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
            } else {
                val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
                with(prefs.edit()) {
                    putInt("budget", budget)
                    putInt("card", card)
                    putInt("cash", cash)
                    putBoolean("first_time", false)
                    apply()
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

    }

}