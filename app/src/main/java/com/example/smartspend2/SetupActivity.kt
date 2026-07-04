package com.example.smartspend2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smartspend2.utils.NumberTextWatcher


class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setup)

        val etBudget: EditText = findViewById(R.id.etBudget)
        val etCard: EditText = findViewById(R.id.etCard)
        val etCash: EditText = findViewById(R.id.etCash)
        val btnContinue: Button = findViewById(R.id.btnContinue)

        etBudget.addTextChangedListener(NumberTextWatcher(etBudget))
        etCard.addTextChangedListener(NumberTextWatcher(etCard))
        etCash.addTextChangedListener(NumberTextWatcher(etCash))

        btnContinue.setOnClickListener {
            val budget = NumberTextWatcher.getCleanValue(etBudget.text.toString()).toIntOrNull()
            val card = NumberTextWatcher.getCleanValue(etCard.text.toString()).toIntOrNull()
            val cash = NumberTextWatcher.getCleanValue(etCash.text.toString()).toIntOrNull()

            if (budget == null || card == null || cash == null) {
                Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
            } else {
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "default"
                val prefs = getSharedPreferences("SmartSpendPrefs_$userId", MODE_PRIVATE)
                with(prefs.edit()) {
                    putInt("budget", budget)
                    putInt("card", card)
                    putInt("cash", cash)
                    putBoolean("first_time", false)
                    apply()
                }

                val profileData = hashMapOf(
                    "budget" to budget,
                    "card" to card,
                    "cash" to cash,
                    "first_time" to false
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("profile")
                    .document("info")
                    .set(profileData)

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

    }

}
