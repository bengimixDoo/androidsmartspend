package com.example.smartspend2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnLogout = view.findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {

        AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất") { _, _ ->

                FirebaseAuth.getInstance().signOut()
                //Xóa SQLite
                val dbHelper = DatabaseHelper(requireContext())
                dbHelper.clearDatabase()

                //Reset first_time
                val prefs = requireContext().getSharedPreferences(
                    "SmartSpendPrefs",
                    android.content.Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putBoolean("first_time", true)
                    .apply()

                //Quay về màn hình đầu tiên
                val intent = Intent(requireContext(), GetStartedActivity::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}