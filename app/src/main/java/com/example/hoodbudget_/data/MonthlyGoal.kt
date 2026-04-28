package com.example.hoodbudget_.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_goals",
    indices = [Index(value = ["username", "month"], unique = true)]
)
data class MonthlyGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val month: String,
    val minAmount: Double,
    val maxAmount: Double
)
