package com.example.hoodbudget_.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allCategories: Flow<List<Category>> = budgetDao.getAllCategories()

    suspend fun insertUser(user: User) = budgetDao.insertUser(user)

    suspend fun getUser(username: String): User? = budgetDao.getUser(username)

    suspend fun insertCategory(category: Category) = budgetDao.insertCategory(category)

    suspend fun deleteCategory(category: Category) = budgetDao.deleteCategory(category)

    suspend fun insertExpense(expenseEntry: ExpenseEntry) = budgetDao.insertExpense(expenseEntry)

    fun getExpensesForPeriod(
        username: String,
        startDate: String,
        endDate: String
    ): Flow<List<ExpenseEntry>> = budgetDao.getExpensesForPeriod(username, startDate, endDate)

    fun getCategoryTotalsForPeriod(
        username: String,
        startDate: String,
        endDate: String
    ): Flow<List<CategorySpendSummary>> =
        budgetDao.getCategoryTotalsForPeriod(username, startDate, endDate)

    fun getTotalSpendForPeriod(
        username: String,
        startDate: String,
        endDate: String
    ): Flow<Double> = budgetDao.getTotalSpendForPeriod(username, startDate, endDate)

    suspend fun insertGoal(goal: MonthlyGoal) = budgetDao.insertGoal(goal)

    fun observeGoal(username: String, month: String): Flow<MonthlyGoal?> =
        budgetDao.observeGoal(username, month)
}
