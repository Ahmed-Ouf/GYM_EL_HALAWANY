package com.example.simplecalc

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class Training(val id: Int, val gameId: Int, val name: String, val description: String = "")

val initialMockTrainings = listOf(
    Training(1, 1, "صدر", "تمارين الصدر المتنوعة"),
    Training(2, 1, "أكتاف", "تمارين الأكتاف بالدمبل والبار"),
    Training(3, 1, "ظهر", "سحب علوي وأرضي"),
    Training(4, 1, "ذراعين", "باي وتراي"),
    Training(5, 1, "أرجل", "سكوات وضغط"),
    Training(6, 1, "بطن", "تمارين البطن الأساسية"),
    Training(7, 3, "تدريب لياقة", "كارديو ولياقة بدنية"),
    Training(8, 3, "كيس الملاكمة", "تدريب على كيس الملاكمة"),
    Training(9, 3, "نزال (سبارينغ)", "مواجهة تطبيقية")
)

val globalTrainings = mutableStateListOf(*initialMockTrainings.toTypedArray())

enum class AdminRoute { Home, GamesList, GameTrainings, TrainingScheduleList, PaymentsList }

@Composable
fun AdminScreen() {
    var currentRoute by remember { mutableStateOf(AdminRoute.Home) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }

    when (currentRoute) {
        AdminRoute.Home -> AdminHomeScreen(
            onNavigateToGames = { currentRoute = AdminRoute.GamesList },
            onNavigateToSchedules = { currentRoute = AdminRoute.TrainingScheduleList },
            onNavigateToPayments = { currentRoute = AdminRoute.PaymentsList }
        )
        AdminRoute.GamesList -> GamesManagementScreen(
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

@Composable
fun AdminHomeScreen(onNavigateToGames: () -> Unit, onNavigateToSchedules: () -> Unit, onNavigateToPayments: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("الإدارة والمدربون", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp, top = 32.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToGames() },
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("إدارة الألعاب (Games)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("إضافة أو تعديل الألعاب (مثل: رفع الأثقال، السباحة).", style = MaterialTheme.typography.bodyMedium)
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToSchedules() },
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("جداول التدريب (Schedules)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("إدارة جداول التدريب الشاملة.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToPayments() },
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("المدفوعات (Payments)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("عرض وتسجيل المدفوعات.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesManagementScreen(onBack: () -> Unit, onGameClick: (Game) -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    var gameToEdit by remember { mutableStateOf<Game?>(null) }

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
            if (globalGames.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد ألعاب حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(globalGames, key = { it.id }) { game ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onGameClick(game) },
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(game.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("السعر: $${game.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    if (game.description.isNotEmpty()) {
                                        Text(game.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
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
        val isEdit = gameToEdit != null
        AddEditGameDialog(
            initialGame = gameToEdit,
            onDismiss = { showAddDialog = false; gameToEdit = null },
            onSave = { name, price, description ->
                if (isEdit) {
                    val index = globalGames.indexOfFirst { it.id == gameToEdit!!.id }
                    if (index != -1) globalGames[index] = gameToEdit!!.copy(name = name, price = price, description = description)
                } else {
                    val newId = (globalGames.maxOfOrNull { it.id } ?: 0) + 1
                    globalGames.add(Game(newId, name, price, description))
                }
                showAddDialog = false
                gameToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTrainingsScreen(game: Game, onBack: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    var trainingToEdit by remember { mutableStateOf<Training?>(null) }
    var trainingToDelete by remember { mutableStateOf<Training?>(null) }

    // Ensure we see updates if the game is edited outside, but we only have its ID
    val currentGame = globalGames.find { it.id == game.id } ?: game
    val gameTrainings = globalTrainings.filter { it.gameId == currentGame.id }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("تدريبات: ${currentGame.name}", fontWeight = FontWeight.Bold) },
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
                icon = { Icon(Icons.Default.Add, "إضافة تدريب") },
                text = { Text("إضافة تدريب") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (gameTrainings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد تدريبات مضافة لهذه اللعبة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(gameTrainings, key = { it.id }) { training ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(training.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (training.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(training.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { trainingToEdit = training }) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { trainingToDelete = training }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || trainingToEdit != null) {
        val isEdit = trainingToEdit != null
        AddEditTrainingDialog(
            initialGame = currentGame,
            initialTraining = trainingToEdit,
            onDismiss = { showAddDialog = false; trainingToEdit = null },
            onSave = { selectedGame, name, description ->
                if (isEdit) {
                    val index = globalTrainings.indexOfFirst { it.id == trainingToEdit!!.id }
                    if (index != -1) globalTrainings[index] = trainingToEdit!!.copy(gameId = selectedGame.id, name = name, description = description)
                } else {
                    val newId = (globalTrainings.maxOfOrNull { it.id } ?: 0) + 1
                    globalTrainings.add(Training(newId, selectedGame.id, name, description))
                }
                showAddDialog = false
                trainingToEdit = null
            }
        )
    }

    if (trainingToDelete != null) {
        AlertDialog(
            onDismissRequest = { trainingToDelete = null },
            title = { Text("حذف التدريب") },
            text = { Text("هل أنت متأكد أنك تريد حذف تدريب '${trainingToDelete!!.name}'؟") },
            confirmButton = {
                TextButton(onClick = {
                    globalTrainings.removeAll { it.id == trainingToDelete!!.id }
                    trainingToDelete = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { trainingToDelete = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun AddEditGameDialog(
    initialGame: Game?,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, description: String) -> Unit
) {
    var name by remember { mutableStateOf(initialGame?.name ?: "") }
    var priceStr by remember { mutableStateOf(initialGame?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(initialGame?.description ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGame != null) "تعديل اللعبة" else "إضافة لعبة جديدة") },
        text = {
            Column {
                if (showError) {
                    Text("يرجى إدخال الاسم والسعر الصحيح.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("اسم اللعبة *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it; showError = false },
                    label = { Text("السعر *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف (اختياري)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedPrice = priceStr.toDoubleOrNull()
                if (name.isBlank() || parsedPrice == null) {
                    showError = true
                } else {
                    onSave(name, parsedPrice, description)
                }
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun AddEditTrainingDialog(
    initialGame: Game,
    initialTraining: Training?,
    onDismiss: () -> Unit,
    onSave: (Game, String, String) -> Unit
) {
    var selectedGame by remember { mutableStateOf(globalGames.find { it.id == initialTraining?.gameId } ?: initialGame) }
    var gameMenuExpanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(initialTraining?.name ?: "") }
    var description by remember { mutableStateOf(initialTraining?.description ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTraining != null) "تعديل التدريب" else "إضافة تدريب جديد") },
        text = {
            Column {
                if (showError) {
                    Text("يرجى إدخال اسم التدريب واختيار اللعبة.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }
                
                Box {
                    OutlinedButton(
                        onClick = { gameMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("اللعبة: ${selectedGame.name}")
                    }
                    DropdownMenu(expanded = gameMenuExpanded, onDismissRequest = { gameMenuExpanded = false }) {
                        globalGames.forEach { g ->
                            DropdownMenuItem(text = { Text(g.name) }, onClick = { selectedGame = g; gameMenuExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("اسم التدريب *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف (اختياري)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) showError = true else onSave(selectedGame, name, description)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("شاشة $title\n(قريباً)", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}