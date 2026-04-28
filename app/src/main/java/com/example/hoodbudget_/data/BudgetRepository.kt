package com.example.hoodbudget_.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allCategories: Flow<List<Category>> = budgetDao.getAllCategories()

    suspend fun insertUser(user: User) = budgetDao.insertUser(user)

    suspend fun getUser(username: String): User? = budgetDao.getUser(username)

    suspend fun insertCategory(category: Category) = budgetDao.insertCategory(category)

    suspend fun deleteCategory(category: Category) = budgetDao.deleteCategory(category)
}
