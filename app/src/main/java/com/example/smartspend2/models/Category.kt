package com.example.smartspend2.models

data class Category(
    val name: String,
    var allocatedAmount: Float,
    var spentAmount: Float = 0f,
    val isExpense: Boolean
)
