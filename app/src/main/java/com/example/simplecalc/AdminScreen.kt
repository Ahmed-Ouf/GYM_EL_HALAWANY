package com.example.simplecalc

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.TrainingTypeEntity
import com.example.simplecalc.ui.AppViewModelProvider
import com.example.simplecalc.ui.components.AppCard
import com.example.simplecalc.ui.components.EmptyState
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import com.example.simplecalc.ui.viewmodel.AdminViewModel

enum class AdminRoute { Home, GamesList, GameTrainings, TrainingScheduleList, PaymentsList }

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var currentRoute by remember { mutableStateOf(AdminRoute.Home) }
    var selectedGame by remember { mutableStateOf<GameEntity?>(null) }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            (fadeIn() + slideInHorizontally { it / 8 }).togetherWith(fadeOut() + slideOutHorizontally { -it / 8 })
        },
        label = "AdminTransition"
    ) { route ->
        when (route) {
            AdminRoute.Home -> AdminHomeScreen(
                onNavigateToGames = { currentRoute = AdminRoute.GamesList },
                onNavigateToSchedules = { currentRoute = AdminRoute.TrainingScheduleList },
                onNavigateToPayments = { currentRoute = AdminRoute.PaymentsList }
            )
            AdminRoute.GamesList -> GamesManagementScreen(
                viewModel = viewModel,
                onBack = { currentRoute = AdminRoute.Home },
                onGameClick = { game ->
                    selectedGame = game
                    currentRoute = AdminRoute.GameTrainings
                }
            )
            AdminRoute.GameTrainings -> {
                if (selectedGame != null) {
                    GameTrainingsScreen(
                        game = selectedGame!!,
                        viewModel = viewModel,
                        onBack = {
                            selectedGame = null
                            currentRoute = AdminRoute.GamesList
                        }
                    )
                } else {
                    currentRoute = AdminRoute.GamesList
                }
            }
            AdminRoute.TrainingScheduleList -> {
                AdminPlaceholderScreen("جداول التدريب الشاملة", onBack = { currentRoute = AdminRoute.Home })
            }
            AdminRoute.PaymentsList -> {
                PaymentsScreen(onBack = { currentRoute = AdminRoute.Home })
            }
        }
    }
}

@Composable
fun AdminHomeScreen(onNavigateToGames: () -> Unit, onNavigateToSchedules: () -> Unit, onNavigateToPayments: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        AppCard(
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = onNavigateToGames
        ) {
            Column {
                Text("إدارة الألعاب (Games)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("إضافة أو تعديل الألعاب (مثل: رفع الأثقال، السباحة).", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        AppCard(
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = onNavigateToSchedules
        ) {
            Column {
                Text("جداول التدريب (Schedules)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("إدارة جداول التدريب الشاملة.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        AppCard(
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = onNavigateToPayments
        ) {
            Column {
                Text("المدفوعات (Payments)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("عرض وتسجيل المدفوعات.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesManagementScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onGameClick: (GameEntity) -> Unit
) {
    val games by viewModel.gamesState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var gameToEdit by remember { mutableStateOf<GameEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("إدارة الألعاب", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, "إضافة لعبة") },
                text = { Text("إضافة لعبة") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (games.isEmpty()) {
                EmptyState(icon = Icons.AutoMirrored.Filled.List, title = "لا توجد ألعاب", subtitle = "لم يتم إضافة أي ألعاب للصالة بعد.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(games, key = { it.id }) { game ->
                        AppCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onClick = { onGameClick(game) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(game.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("السعر الافتراضي: $${game.defaultPrice}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { gameToEdit = game }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || gameToEdit != null) {
        val initialGame = gameToEdit
        AddEditGameDialog(
            initialGame = initialGame,
            onDismiss = {
                showAddDialog = false
                gameToEdit = null
            },
            onSave = { name, price ->
                if (initialGame != null) {
                    viewModel.updateGame(initialGame.copy(name = name, defaultPrice = price))
                } else {
                    viewModel.addGame(name, price)
                }
                showAddDialog = false
                gameToEdit = null
            }
        )
    }
}

@Composable
fun AddEditGameDialog(
    initialGame: GameEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialGame?.name ?: "") }
    var priceStr by remember { mutableStateOf(initialGame?.defaultPrice?.toString() ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGame != null) "تعديل اللعبة" else "إضافة لعبة جديدة") },
        text = {
            Column {
                if (showError) {
                    Text("اسم اللعبة والسعر مطلوبان.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("اسم اللعبة *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it; showError = false },
                    label = { Text("السعر الافتراضي *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceStr.toDoubleOrNull()
                    if (name.isBlank() || p == null) {
                        showError = true
                    } else {
                        onSave(name, p)
                    }
                }
            ) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTrainingsScreen(
    game: GameEntity,
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val trainingTypes by viewModel.getTrainingTypesForGame(game.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("تدريبات: ${game.name}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, "إضافة تدريب") },
                text = { Text("إضافة تدريب") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (trainingTypes.isEmpty()) {
                EmptyState(icon = Icons.AutoMirrored.Filled.List, title = "لا توجد تدريبات", subtitle = "لم يتم إضافة أنواع تدريب لهذه اللعبة بعد.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(trainingTypes, key = { it.id }) { type ->
                        AppCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.name, style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { viewModel.deleteTrainingType(type) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة نوع تدريب جديد") },
            text = {
                Column {
                    if (showError) Text("اسم التدريب مطلوب.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; showError = false },
                        label = { Text("اسم التدريب * (مثل: صدر، ظهر)") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) showError = true else {
                        viewModel.addTrainingType(game.id, name)
                        showAddDialog = false
                    }
                }) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            EmptyState(icon = Icons.AutoMirrored.Filled.List, title = title, subtitle = "قسم تحت التطوير للإدارة الشاملة.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
    SimpleCalcTheme {
        AdminHomeScreen(
            onNavigateToGames = {},
            onNavigateToSchedules = {},
            onNavigateToPayments = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    SimpleCalcTheme {
        AdminScreen()
    }
}