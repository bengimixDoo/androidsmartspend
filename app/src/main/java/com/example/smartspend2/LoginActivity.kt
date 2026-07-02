package com.example.smartspend2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // Import thư viện của Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    // 1. Khai báo biến FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 2. Khởi tạo đối tượng FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Khởi tạo các View
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            handleLogin()
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Xác thực cục bộ (Giữ nguyên)
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email không đúng định dạng"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Vui lòng nhập mật khẩu"
            etPassword.requestFocus()
            return
        }

        Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show()

        // 3. Sử dụng Firebase để Đăng nhập
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Nhánh thành công
                    Toast.makeText(baseContext, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                    // Firebase tự động quản lý phiên làm việc (Session/Token) cho bạn.
                    // Không cần dùng SharedPreferences để lưu JWT bằng tay nữa.

                    // Chuyển hướng
                    val intent = Intent(this, SetupActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Nhánh thất bại (Sai pass, tài khoản chưa tồn tại...)
                    // task.exception?.message sẽ in ra nguyên nhân lỗi cụ thể từ hệ thống Firebase
                    Toast.makeText(baseContext, "Lỗi đăng nhập: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}