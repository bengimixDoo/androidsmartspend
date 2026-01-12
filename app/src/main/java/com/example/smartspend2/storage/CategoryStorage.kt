package com.example.smartspend2.storage

import android.content.Context
import com.example.smartspend2.models.Category
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CategoryStorage {
    private const val PREF_NAME = "SmartSpendPrefs"
    private const val KEY_CATEGORIES = "categories"

    fun saveCategories(context: Context, categories: List<Category>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(categories)
        editor.putString(KEY_CATEGORIES, json)
        editor.apply()
    }

    fun loadCategories(context: Context): MutableList<Category> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CATEGORIES, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Category>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
