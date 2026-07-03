package com.example.smartspend2.models

data class Category(
    val id: Long = 0,
    val name: String = "",
    var allocatedAmount: Float = 0f,
    var spentAmount: Float = 0f,
    val isExpense: Boolean = true,
    val key: String? = null
)
