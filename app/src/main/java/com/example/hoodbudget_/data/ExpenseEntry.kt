package com.example.hoodbudget_.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val amount: Double,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val categoryName: String,
    val photoUri: String? = null
)
