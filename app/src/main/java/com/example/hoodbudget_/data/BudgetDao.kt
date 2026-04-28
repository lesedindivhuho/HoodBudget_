package com.example.hoodbudget_.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    // User
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): User?

    // Category
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Delete
    suspend fun deleteCategory(category: Category)

    // Expenses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expenseEntry: ExpenseEntry)

    @Query(
        "SELECT * FROM expenses " +
            "WHERE username = :username AND date BETWEEN :startDate AND :endDate " +
            "ORDER BY date DESC, startTime DESC"
    )
    fun getExpensesForPeriod(
        username: String,
        startDate: String,
        endDate: String
    ): Flow<List<ExpenseEntry>>

    @Query(
        "SELECT categoryName, COALESCE(SUM(amount), 0) AS totalAmount FROM expenses " +
            "WHERE username = :username AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY categoryName ORDER BY totalAmount DESC"
    )
    fun getCategoryTotalsForPeriod(
        username: String,
        startDate: String,
        endDate: String
    ): Flow<List<CategorySpendSummary>>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
            "WHERE username = :username AND date BETWEEN :startDate AND :endDate"
    )
    fun getTotalSpendForPeriod(
        username: String,
        startDate: String,
        endDate: String
    ): Flow<Double>

    // Monthly goals
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: MonthlyGoal)

    @Query("SELECT * FROM monthly_goals WHERE username = :username AND month = :month LIMIT 1")
    fun observeGoal(username: String, month: String): Flow<MonthlyGoal?>
}
