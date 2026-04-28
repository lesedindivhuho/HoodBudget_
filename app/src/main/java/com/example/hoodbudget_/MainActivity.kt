package com.example.hoodbudget_

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.hoodbudget_.data.AppDatabase
import com.example.hoodbudget_.data.BudgetRepository
import com.example.hoodbudget_.ui.BudgetViewModel
import com.example.hoodbudget_.ui.BudgetViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = BudgetRepository(database.budgetDao())
        val factory = BudgetViewModelFactory(repository)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BudgetApp(factory)
                }
            }
        }
    }
}

@Composable
fun BudgetApp(factory: BudgetViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: BudgetViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("categories") },
                viewModel = viewModel
            )
        }
        composable("categories") {
            CategoryScreen(
                viewModel = viewModel,
                onNavigateToExpenses = { navController.navigate("expenses") }
            )
        }
        composable("expenses") {
            ExpenseScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, viewModel: BudgetViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "HoodBudget Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (errorMsg.isNotEmpty()) {
            Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                scope.launch {
                    if (viewModel.login(username, password)) {
                        onLoginSuccess()
                    } else {
                        errorMsg = "Invalid password"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login / Register")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(viewModel: BudgetViewModel, onNavigateToExpenses: () -> Unit) {
    val categories by viewModel.allCategories.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                actions = {
                    IconButton(onClick = onNavigateToExpenses) {
                        Icon(Icons.Default.List, contentDescription = "Expenses")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("New Category") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCategory(newCategoryName)
                        newCategoryName = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(categories) { category ->
                    ListItem(
                        headlineContent = { Text(category.name) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: BudgetViewModel, onBack: () -> Unit) {
    val expenses by viewModel.allExpenses.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var selectedCategory by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    
    var expanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> photoUri = uri?.toString() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Add New Expense", style = MaterialTheme.typography.titleMedium)
            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(value = date, onValueChange = { date = it }, label = { Text("Date (yyyy-MM-dd)") }, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time") }, modifier = Modifier.weight(1f))
                TextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End Time") }, modifier = Modifier.weight(1f))
            }
            
            Box {
                Text(
                    text = if (selectedCategory.isEmpty()) "Select Category" else "Category: $selectedCategory",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .padding(vertical = 12.dp)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category.name
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { 
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Add Photo")
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Selected Photo",
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Button(
                onClick = {
                    if (description.isNotBlank() && selectedCategory.isNotBlank()) {
                        viewModel.addExpense(date, startTime, endTime, description, selectedCategory, photoUri)
                        description = ""
                        photoUri = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("All Expenses", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(expenses) { expense ->
                    ListItem(
                        headlineContent = { Text("${expense.description} (${expense.categoryName})") },
                        supportingContent = { 
                            Column {
                                Text("${expense.date} | ${expense.startTime} - ${expense.endTime}")
                                if (expense.photoUri != null) {
                                    AsyncImage(
                                        model = expense.photoUri,
                                        contentDescription = "Expense Photo",
                                        modifier = Modifier.size(100.dp).padding(top = 8.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    )
                }
            }
        }
    }
}
