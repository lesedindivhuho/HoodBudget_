package com.example.hoodbudget_.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hoodbudget_.data.BudgetRepository
import com.example.hoodbudget_.data.Category
import com.example.hoodbudget_.data.CategorySpendSummary
import com.example.hoodbudget_.data.ExpenseEntry
import com.example.hoodbudget_.data.MonthlyGoal
import com.example.hoodbudget_.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DateRange(
    val startDate: String,
    val endDate: String
)

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val currentUsername = MutableStateFlow<String?>(null)
    private val expenseRange = MutableStateFlow(defaultCurrentMonthRange())
    private val goalMonth = MutableStateFlow(currentMonthKey())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<String?> = currentUsername

    val selectedExpenseRange: StateFlow<DateRange> = expenseRange

    val selectedGoalMonth: StateFlow<String> = goalMonth

    val filteredExpenses: StateFlow<List<ExpenseEntry>> =
        combine(currentUsername, expenseRange) { username, range -> username to range }
            .flatMapLatest { (username, range) ->
                if (username == null) {
                    flowOf(emptyList())
                } else {
                    repository.getExpensesForPeriod(username, range.startDate, range.endDate)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryTotals: StateFlow<List<CategorySpendSummary>> =
        combine(currentUsername, expenseRange) { username, range -> username to range }
            .flatMapLatest { (username, range) ->
                if (username == null) {
                    flowOf(emptyList())
                } else {
                    repository.getCategoryTotalsForPeriod(username, range.startDate, range.endDate)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGoal: StateFlow<MonthlyGoal?> =
        combine(currentUsername, goalMonth) { username, month -> username to month }
            .flatMapLatest { (username, month) ->
                if (username == null) {
                    flowOf(null)
                } else {
                    repository.observeGoal(username, month)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val goalMonthSpend: StateFlow<Double> =
        combine(currentUsername, goalMonth) { username, month -> username to month }
            .flatMapLatest { (username, month) ->
                if (username == null) {
                    flowOf(0.0)
                } else {
                    val range = monthToDateRange(month)
                    repository.getTotalSpendForPeriod(username, range.startDate, range.endDate)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    suspend fun login(username: String, password: String): Boolean {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
            return false
        }

        val user = repository.getUser(trimmedUsername)
        val success = user != null && user.password == trimmedPassword

        if (success) {
            currentUsername.value = trimmedUsername
        }
        return success
    }

    suspend fun register(username: String, password: String): Boolean {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
            return false
        }

        val existingUser = repository.getUser(trimmedUsername)
        if (existingUser != null) {
            return false
        }

        repository.insertUser(User(trimmedUsername, trimmedPassword))
        currentUsername.value = trimmedUsername
        return true
    }

    fun logout() {
        currentUsername.value = null
    }

    fun addCategory(name: String) {
        val categoryName = name.trim()
        if (categoryName.isBlank()) return
        viewModelScope.launch {
            repository.insertCategory(Category(categoryName))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun updateExpenseRange(startDate: String, endDate: String) {
        expenseRange.update { DateRange(startDate.trim(), endDate.trim()) }
    }

    fun updateGoalMonth(month: String) {
        goalMonth.value = month.trim()
    }

    fun addExpense(
        amount: Double,
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        categoryName: String,
        photoUri: String?
    ) {
        val username = currentUsername.value ?: return
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntry(
                    username = username,
                    amount = amount,
                    date = date.trim(),
                    startTime = startTime.trim(),
                    endTime = endTime.trim(),
                    description = description.trim(),
                    categoryName = categoryName.trim(),
                    photoUri = photoUri
                )
            )
        }
    }

    fun saveGoal(minAmount: Double, maxAmount: Double) {
        val username = currentUsername.value ?: return
        val month = goalMonth.value
        val existingGoalId = activeGoal.value?.id ?: 0
        viewModelScope.launch {
            repository.insertGoal(
                MonthlyGoal(
                    id = existingGoalId,
                    username = username,
                    month = month,
                    minAmount = minAmount,
                    maxAmount = maxAmount
                )
            )
        }
    }
}

class BudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun currentMonthKey(): String {
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    return "%04d-%02d".format(year, month)
}

private fun defaultCurrentMonthRange(): DateRange = monthToDateRange(currentMonthKey())

private fun monthToDateRange(month: String): DateRange {
    val year = month.substringBefore("-").toIntOrNull() ?: java.util.Calendar.getInstance()
        .get(java.util.Calendar.YEAR)
    val monthNumber = month.substringAfter("-", "1").toIntOrNull()?.coerceIn(1, 12) ?: 1
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, year)
        set(java.util.Calendar.MONTH, monthNumber - 1)
        set(java.util.Calendar.DAY_OF_MONTH, 1)
    }
    val startDate = "%04d-%02d-01".format(year, monthNumber)
    val lastDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val endDate = "%04d-%02d-%02d".format(year, monthNumber, lastDay)
    return DateRange(startDate, endDate)
}
