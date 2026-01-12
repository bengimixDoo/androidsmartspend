package com.example.smartspend2.models

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Float,
    val category: String,
    val date: String,
    val isExpense: Boolean
)
