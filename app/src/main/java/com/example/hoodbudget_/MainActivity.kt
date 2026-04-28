package com.example.hoodbudget_

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hoodbudget_.data.AppDatabase
import com.example.hoodbudget_.data.BudgetRepository
import com.example.hoodbudget_.data.Category
import com.example.hoodbudget_.data.CategorySpendSummary
import com.example.hoodbudget_.data.ExpenseEntry
import com.example.hoodbudget_.data.MonthlyGoal
import com.example.hoodbudget_.ui.BudgetViewModel
import com.example.hoodbudget_.ui.BudgetViewModelFactory
import com.example.hoodbudget_.ui.DateRange
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = BudgetRepository(database.budgetDao())
        val factory = BudgetViewModelFactory(repository)

        setContent {
            HoodBudgetTheme {
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

private object Routes {
    const val Splash = "splash"
    const val AuthHome = "auth_home"
    const val Login = "login"
    const val Register = "register"
    const val Dashboard = "dashboard"
}

@Composable
fun BudgetApp(factory: BudgetViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: BudgetViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.AuthHome) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.AuthHome) {
            AuthChoiceScreen(
                onLogin = { navController.navigate(Routes.Login) },
                onRegister = { navController.navigate(Routes.Register) }
            )
        }
        composable(Routes.Login) {
            LoginScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.AuthHome) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.AuthHome) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Dashboard) {
            DashboardScreen(viewModel = viewModel, navController = navController)
        }
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 5000, easing = LinearEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF4FB), Color(0xFFFBD1E8), Color(0xFF23141D))
                )
            )
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BudgetLogo(modifier = Modifier.size(180.dp))
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "HoodBudget",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track every move with style.",
                color = Color(0xFFFFDAED),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFFFF5FA7),
                trackColor = Color.White.copy(alpha = 0.24f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Loading your budget space...",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AuthChoiceScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF7FC), Color(0xFFFFE3F1), Color(0xFFF9EEF5))
                )
            )
            .padding(24.dp)
    ) {
        PremiumCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BudgetLogo(modifier = Modifier.size(132.dp))
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Welcome to HoodBudget",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2A1823)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Create your account first, then log in with details saved on your device.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                PrimaryPinkButton(text = "Create Account", onClick = onRegister)
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryDarkButton(text = "Log In", onClick = onLogin)
            }
        }
    }
}

@Composable
private fun LoginScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AuthScreenShell(
        title = "Log In",
        subtitle = "Only locally saved accounts can log in.",
        onBack = onBack
    ) {
        OutlinedInputField(value = username, onValueChange = { username = it }, label = "Username")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedInputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )
        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(18.dp))
        PrimaryPinkButton(
            text = "Log In",
            onClick = {
                scope.launch {
                    if (viewModel.login(username, password)) {
                        errorMsg = ""
                        onLoginSuccess()
                    } else {
                        errorMsg = "That account does not exist locally or the password is wrong."
                    }
                }
            }
        )
    }
}

@Composable
private fun RegisterScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AuthScreenShell(
        title = "Create Account",
        subtitle = "Save your details locally before using the app.",
        onBack = onBack
    ) {
        OutlinedInputField(value = username, onValueChange = { username = it }, label = "Username")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedInputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedInputField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isPassword = true
        )
        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(18.dp))
        PrimaryPinkButton(
            text = "Create Account",
            onClick = {
                scope.launch {
                    errorMsg = when {
                        username.isBlank() || password.isBlank() -> "Fill in all fields."
                        password != confirmPassword -> "Passwords do not match."
                        !viewModel.register(username, password) -> "That username already exists locally."
                        else -> {
                            onRegisterSuccess()
                            ""
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun AuthScreenShell(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF8FC), Color(0xFFFFE6F2), Color(0xFFF9EFF5))
                )
            )
            .padding(24.dp)
    ) {
        PremiumCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Column {
                TextButton(onClick = onBack) {
                    Text("Back", color = Color(0xFF2A1823))
                }
                Spacer(modifier = Modifier.height(2.dp))
                BudgetLogo(modifier = Modifier.size(112.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2A1823)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(20.dp))
                content()
            }
        }
    }
}

private enum class BudgetTab(val label: String, val iconRes: Int) {
    Expenses("Expenses", android.R.drawable.ic_menu_edit),
    Categories("Categories", android.R.drawable.ic_menu_sort_by_size),
    Goals("Goals", android.R.drawable.star_big_on),
    Summary("Summary", android.R.drawable.ic_menu_agenda)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    viewModel: BudgetViewModel,
    navController: NavHostController
) {
    val categories by viewModel.allCategories.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val expenseRange by viewModel.selectedExpenseRange.collectAsState()
    val goalMonth by viewModel.selectedGoalMonth.collectAsState()
    val activeGoal by viewModel.activeGoal.collectAsState()
    val goalMonthSpend by viewModel.goalMonthSpend.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(BudgetTab.Expenses) }
    var selectedPhotoPreviewUri by remember { mutableStateOf<String?>(null) }

    val expensesScroll = rememberScrollState()
    val categoriesScroll = rememberScrollState()
    val goalsScroll = rememberScrollState()
    val summaryScroll = rememberScrollState()
    val totalSpend = categoryTotals.sumOf(CategorySpendSummary::totalAmount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFFE4F1))
                            .border(1.dp, Color(0xFFF5B7D7), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "HoodBudget",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF24111B),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = currentUser.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFC13F82),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.logout()
                            navController.navigate(Routes.AuthHome) {
                                popUpTo(Routes.Dashboard) { inclusive = true }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A1823),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFF9ECB))
                    ) {
                        Text("Logout", fontWeight = FontWeight.Black)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFFFBFE),
                tonalElevation = 8.dp
            ) {
                BudgetTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                BudgetTab.Expenses -> ExpensesTab(
                    categories = categories,
                    expenses = expenses,
                    range = expenseRange,
                    scrollState = expensesScroll,
                    categoriesCount = categories.size,
                    entriesCount = expenses.size,
                    totalSpend = totalSpend,
                    onApplyRange = viewModel::updateExpenseRange,
                    onAddExpense = viewModel::addExpense,
                    onPreviewPhoto = { selectedPhotoPreviewUri = it }
                )
                BudgetTab.Categories -> CategoriesTab(
                    categories = categories,
                    scrollState = categoriesScroll,
                    categoriesCount = categories.size,
                    entriesCount = expenses.size,
                    totalSpend = totalSpend,
                    onAddCategory = viewModel::addCategory,
                    onDeleteCategory = viewModel::deleteCategory
                )
                BudgetTab.Goals -> GoalsTab(
                    goalMonth = goalMonth,
                    activeGoal = activeGoal,
                    totalForGoalMonth = goalMonthSpend,
                    scrollState = goalsScroll,
                    categoriesCount = categories.size,
                    entriesCount = expenses.size,
                    totalSpend = totalSpend,
                    onMonthChange = viewModel::updateGoalMonth,
                    onSaveGoal = viewModel::saveGoal
                )
                BudgetTab.Summary -> SummaryTab(
                    categoryTotals = categoryTotals,
                    range = expenseRange,
                    scrollState = summaryScroll,
                    categoriesCount = categories.size,
                    entriesCount = expenses.size,
                    totalSpend = totalSpend
                )
            }
        }
    }

    selectedPhotoPreviewUri?.let { uri ->
        PhotoPreviewDialog(uriString = uri, onDismiss = { selectedPhotoPreviewUri = null })
    }
}

@Composable
private fun DashboardHeader(
    categories: Int,
    entries: Int,
    totalSpend: Double,
    scrollOffset: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = -(scrollOffset * 0.28f)
                alpha = (1f - (scrollOffset / 1400f)).coerceIn(0.72f, 1f)
            }
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFD0E8), Color(0xFFFF8BC1), Color(0xFF352030))
                )
            )
            .clip(RoundedCornerShape(30.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BudgetLogo(modifier = Modifier.size(84.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Track smarter. Spend prettier.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A softer look, cleaner type, and all your budget data stored offline.",
                        color = Color(0xFFFFE5F2)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Categories", categories.toString(), Modifier.weight(1f))
                StatCard("Entries", entries.toString(), Modifier.weight(1f))
                StatCard("This Period", formatCurrency(totalSpend), Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpensesTab(
    categories: List<Category>,
    expenses: List<ExpenseEntry>,
    range: DateRange,
    scrollState: androidx.compose.foundation.ScrollState,
    categoriesCount: Int,
    entriesCount: Int,
    totalSpend: Double,
    onApplyRange: (String, String) -> Unit,
    onAddExpense: (Double, String, String, String, String, String, String?) -> Unit,
    onPreviewPhoto: (String) -> Unit
) {
    val context = LocalContext.current
    var amountText by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(todayDate()) }
    var startTime by rememberSaveable { mutableStateOf("08:00") }
    var endTime by rememberSaveable { mutableStateOf("09:00") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(categories.firstOrNull()?.name.orEmpty()) }
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf("") }
    var filterStart by rememberSaveable { mutableStateOf(range.startDate) }
    var filterEnd by rememberSaveable { mutableStateOf(range.endDate) }

    LaunchedEffect(categories) {
        if (selectedCategory.isBlank() && categories.isNotEmpty()) {
            selectedCategory = categories.first().name
        }
    }
    LaunchedEffect(range.startDate, range.endDate) {
        filterStart = range.startDate
        filterEnd = range.endDate
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            photoUri = it.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DashboardHeader(
            categories = categoriesCount,
            entries = entriesCount,
            totalSpend = totalSpend,
            scrollOffset = scrollState.value
        )

        PremiumCard(title = "Add Expense", subtitle = "Every field should feel clear and easy to use.") {
            OutlinedInputField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "Amount",
                keyboardType = KeyboardType.Decimal
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedInputField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Date",
                    modifier = Modifier.weight(1f)
                )
                OutlinedInputField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = "Start",
                    modifier = Modifier.weight(1f)
                )
                OutlinedInputField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = "End",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedInputField(
                value = description,
                onValueChange = { description = it },
                label = "Description"
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text("Pick a category", fontWeight = FontWeight.Bold, color = Color(0xFF2A1823))
            Spacer(modifier = Modifier.height(8.dp))
            if (categories.isEmpty()) {
                Text("Create at least one category first.", color = MaterialTheme.colorScheme.error)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category.name,
                            onClick = { selectedCategory = category.name },
                            label = { Text(category.name) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SecondaryDarkButton(
                    text = if (photoUri == null) "Attach Photo" else "Change Photo",
                    onClick = { photoPicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                photoUri?.let {
                    PhotoThumbnail(uriString = it, modifier = Modifier.size(66.dp))
                }
            }
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    color = if (message.startsWith("Saved")) Color(0xFF2E7A52) else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryPinkButton(
                text = "Save Expense",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    message = when {
                        categories.isEmpty() -> "Create a category first."
                        amount == null || amount <= 0 -> "Enter a valid amount."
                        !isValidDate(date) -> "Use date format YYYY-MM-DD."
                        !isValidTime(startTime) || !isValidTime(endTime) -> "Use time format HH:MM."
                        description.isBlank() -> "Add a short description."
                        selectedCategory.isBlank() -> "Pick a category."
                        else -> {
                            onAddExpense(
                                amount,
                                date,
                                startTime,
                                endTime,
                                description,
                                selectedCategory,
                                photoUri
                            )
                            amountText = ""
                            description = ""
                            photoUri = null
                            "Saved expense successfully."
                        }
                    }
                }
            )
        }

        PremiumCard(title = "Filter Period", subtitle = "Choose exactly which entries and totals should show.") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedInputField(
                    value = filterStart,
                    onValueChange = { filterStart = it },
                    label = "Start date",
                    modifier = Modifier.weight(1f)
                )
                OutlinedInputField(
                    value = filterEnd,
                    onValueChange = { filterEnd = it },
                    label = "End date",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            SecondaryDarkButton(
                text = "Apply Range",
                onClick = {
                    message = if (isValidDate(filterStart) && isValidDate(filterEnd)) {
                        onApplyRange(filterStart, filterEnd)
                        "Saved filter range."
                    } else {
                        "Use YYYY-MM-DD for both dates."
                    }
                }
            )
        }

        PremiumCard(
            title = "Expense History",
            subtitle = "Showing ${expenses.size} entries from ${range.startDate} to ${range.endDate}."
        ) {
            if (expenses.isEmpty()) {
                Text("No expenses match this period yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    expenses.forEach { expense ->
                        ExpenseCard(expense = expense, onPreviewPhoto = onPreviewPhoto)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriesTab(
    categories: List<Category>,
    scrollState: androidx.compose.foundation.ScrollState,
    categoriesCount: Int,
    entriesCount: Int,
    totalSpend: Double,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    var newCategoryName by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DashboardHeader(categoriesCount, entriesCount, totalSpend, scrollState.value)
        PremiumCard(title = "Categories", subtitle = "Keep your spending groups clean and intentional.") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedInputField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = "New category",
                    modifier = Modifier.weight(1f)
                )
                PrimaryPinkButton(
                    text = "Add",
                    modifier = Modifier.width(96.dp),
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategory(newCategoryName)
                            newCategoryName = ""
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            if (categories.isEmpty()) {
                Text("No categories yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categories.forEach { category ->
                        FancyListCard(
                            title = category.name,
                            actionText = "Delete",
                            onAction = { onDeleteCategory(category) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsTab(
    goalMonth: String,
    activeGoal: MonthlyGoal?,
    totalForGoalMonth: Double,
    scrollState: androidx.compose.foundation.ScrollState,
    categoriesCount: Int,
    entriesCount: Int,
    totalSpend: Double,
    onMonthChange: (String) -> Unit,
    onSaveGoal: (Double, Double) -> Unit
) {
    var month by rememberSaveable { mutableStateOf(goalMonth) }
    var minAmount by rememberSaveable { mutableStateOf("") }
    var maxAmount by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(goalMonth, activeGoal?.id) {
        month = goalMonth
        minAmount = activeGoal?.minAmount?.toCurrencyText().orEmpty()
        maxAmount = activeGoal?.maxAmount?.toCurrencyText().orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DashboardHeader(categoriesCount, entriesCount, totalSpend, scrollState.value)
        PremiumCard(title = "Monthly Goals", subtitle = "Set both the floor and ceiling for your spending.") {
            OutlinedInputField(
                value = month,
                onValueChange = { month = it },
                label = "Month (YYYY-MM)"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedInputField(
                    value = minAmount,
                    onValueChange = { minAmount = it },
                    label = "Minimum",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                OutlinedInputField(
                    value = maxAmount,
                    onValueChange = { maxAmount = it },
                    label = "Maximum",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryDarkButton(
                    text = "Load Month",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        message = if (isValidMonth(month)) {
                            onMonthChange(month)
                            "Loaded $month."
                        } else {
                            "Use month format YYYY-MM."
                        }
                    }
                )
                PrimaryPinkButton(
                    text = "Save Goal",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val min = minAmount.toDoubleOrNull()
                        val max = maxAmount.toDoubleOrNull()
                        message = when {
                            !isValidMonth(month) -> "Use month format YYYY-MM."
                            min == null || max == null -> "Enter valid minimum and maximum amounts."
                            min > max -> "Minimum goal cannot be higher than maximum goal."
                            else -> {
                                onMonthChange(month)
                                onSaveGoal(min, max)
                                "Goal saved for $month."
                            }
                        }
                    }
                )
            }
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        PremiumCard(title = "Goal Snapshot", subtitle = "See whether your current month is on track.") {
            Text("Selected month: $goalMonth", fontWeight = FontWeight.Bold, color = Color(0xFF2A1823))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Spent so far: ${formatCurrency(totalForGoalMonth)}", color = Color(0xFF2A1823))
            Spacer(modifier = Modifier.height(8.dp))
            if (activeGoal == null) {
                Text("No goal saved for this month yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Minimum goal: ${formatCurrency(activeGoal.minAmount)}", color = Color(0xFF2A1823))
                Text("Maximum goal: ${formatCurrency(activeGoal.maxAmount)}", color = Color(0xFF2A1823))
                Spacer(modifier = Modifier.height(12.dp))
                GoalStatusChip(totalForGoalMonth = totalForGoalMonth, goal = activeGoal)
            }
        }
    }
}

@Composable
private fun SummaryTab(
    categoryTotals: List<CategorySpendSummary>,
    range: DateRange,
    scrollState: androidx.compose.foundation.ScrollState,
    categoriesCount: Int,
    entriesCount: Int,
    totalSpend: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DashboardHeader(categoriesCount, entriesCount, totalSpend, scrollState.value)
        PremiumCard(
            title = "Category Totals",
            subtitle = "Totals from ${range.startDate} to ${range.endDate}."
        ) {
            if (categoryTotals.isEmpty()) {
                Text("No category totals available for this range yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categoryTotals.forEach { total ->
                        FancySummaryRow(title = total.categoryName, value = formatCurrency(total.totalAmount))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseEntry, onPreviewPhoto: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF4BED9)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(expense.categoryName, fontWeight = FontWeight.Black, color = Color(0xFF2A1823))
                Text(formatCurrency(expense.amount), color = Color(0xFFD3458A), fontWeight = FontWeight.Black)
            }
            Text(expense.description, fontWeight = FontWeight.SemiBold, color = Color(0xFF2A1823))
            Text(
                text = "${expense.date}  ${expense.startTime} - ${expense.endTime}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            expense.photoUri?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PhotoThumbnail(uriString = it, modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    SecondaryDarkButton(text = "View Photo", onClick = { onPreviewPhoto(it) })
                }
            }
        }
    }
}

@Composable
private fun GoalStatusChip(totalForGoalMonth: Double, goal: MonthlyGoal) {
    val (label, color) = when {
        totalForGoalMonth < goal.minAmount -> "Below minimum" to Color(0xFFA65B13)
        totalForGoalMonth > goal.maxAmount -> "Above maximum" to Color(0xFFB13161)
        else -> "Within range" to Color(0xFF2E7A52)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(100))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0xFFF3C4DD)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF8FC))))
                .padding(18.dp)
        ) {
            title?.let {
                Text(it, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF2A1823))
            }
            subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
            }
            content()
        }
    }
}

@Composable
private fun FancyListCard(title: String, actionText: String, onAction: () -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5FB)),
        border = BorderStroke(1.dp, Color(0xFFF5BEDB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF2A1823))
            TextButton(onClick = onAction) {
                Text(actionText, color = Color(0xFF2A1823))
            }
        }
    }
}

@Composable
private fun FancySummaryRow(title: String, value: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8FC)),
        border = BorderStroke(1.dp, Color(0xFFF2C8DE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF2A1823))
            Text(value, fontWeight = FontWeight.Black, color = Color(0xFFD3458A))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color(0xFF7C4A67))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Black, color = Color(0xFF2A1823))
        }
    }
}

@Composable
private fun OutlinedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Color(0xFF1E1018),
            fontWeight = FontWeight.SemiBold
        ),
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) android.R.drawable.presence_online
                            else android.R.drawable.presence_invisible
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFFC94D8E)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF1E1018),
            unfocusedTextColor = Color(0xFF1E1018),
            focusedBorderColor = Color(0xFFFF5FA7),
            unfocusedBorderColor = Color(0xFFE3AFCB),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Color(0xFFFF5FA7),
            focusedLabelColor = Color(0xFFC94D8E),
            unfocusedLabelColor = Color(0xFF8D617A)
        )
    )
}

@Composable
private fun PrimaryPinkButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF5FA7),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SecondaryDarkButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2A1823),
            contentColor = Color(0xFFFFD4E9)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PhotoThumbnail(uriString: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, uriString) {
        value = try {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Expense photo",
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, Color(0xFFF3C2DB), RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFFF0F8))
                .border(1.dp, Color(0xFFF3C2DB), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Photo", fontWeight = FontWeight.Bold, color = Color(0xFF2A1823))
        }
    }
}

@Composable
private fun PhotoPreviewDialog(uriString: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF2A1823))
            }
        },
        text = {
            PhotoThumbnail(
                uriString = uriString,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }
    )
}

@Composable
private fun BudgetLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.hoodbudget_logo),
        contentDescription = "HoodBudget logo",
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun HoodBudgetTheme(content: @Composable () -> Unit) {
    val lightScheme = lightColorScheme(
        primary = Color(0xFFFF5FA7),
        onPrimary = Color.White,
        secondary = Color(0xFF2A1823),
        onSecondary = Color(0xFFFFD4E9),
        tertiary = Color(0xFFFF9AC8),
        background = Color(0xFFFFF6FB),
        surface = Color.White,
        onSurface = Color(0xFF20121A),
        onSurfaceVariant = Color(0xFF7E5C70),
        error = Color(0xFFB13161)
    )
    val darkScheme = darkColorScheme(
        primary = Color(0xFFFF7AB8),
        onPrimary = Color.White,
        secondary = Color(0xFFFFD2E7),
        onSecondary = Color(0xFF1E1218),
        background = Color(0xFF171118),
        surface = Color(0xFF221821),
        onSurface = Color(0xFFFFF4FA),
        onSurfaceVariant = Color(0xFFE0C5D4)
    )

    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkScheme else lightScheme,
        content = content
    )
}

private fun formatCurrency(amount: Double): String = NumberFormat.getCurrencyInstance().format(amount)

private fun isValidDate(value: String): Boolean = Regex("""\d{4}-\d{2}-\d{2}""").matches(value)

private fun isValidTime(value: String): Boolean = Regex("""\d{2}:\d{2}""").matches(value)

private fun isValidMonth(value: String): Boolean = Regex("""\d{4}-\d{2}""").matches(value)

private fun Double.toCurrencyText(): String = if (this % 1.0 == 0.0) this.toInt().toString() else this.toString()

private fun todayDate(): String {
    val calendar = Calendar.getInstance()
    return "%04d-%02d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
