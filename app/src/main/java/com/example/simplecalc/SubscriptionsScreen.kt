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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp

data class Game(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String = ""
)

val initialMockGames = listOf(
    Game(1, "رفع الأثقال", 500.0, "تمارين رفع الأثقال وكمال الأجسام"),
    Game(2, "سباحة", 400.0, "تدريب السباحة الحر"),
    Game(3, "ملاكمة", 600.0, "فنون قتالية وملاكمة")
)

val globalGames = mutableStateListOf(*initialMockGames.toTypedArray())

data class Subscription(
    val id: Int,
    val memberId: Int,
    val memberName: String,
    val gameId: Int,
    val gameName: String,
    val planType: String,
    val startDate: String,
    val endDate: String,
    val price: Double,
    val totalSessions: Int,
    val status: String
)

val initialMockSubscriptions = listOf(
    Subscription(1, 1, "أحمد محمد", 1, "رفع الأثقال", "شهري", "2024-10-01", "2024-11-01", 500.0, 30, "منتهي"),
    Subscription(2, 2, "سارة أحمد", 2, "سباحة", "سنوي", "2024-01-01", "2024-12-31", 400.0, 100, "نشط"),
    Subscription(3, 3, "خالد محمود", 3, "ملاكمة", "3 أشهر", "2024-12-01", "2025-03-01", 1800.0, 36, "قادم"),
    Subscription(4, 1, "أحمد محمد", 2, "سباحة", "شهري", "2024-10-01", "2024-11-01", 400.0, 12, "نشط")
)

val globalSubscriptions = mutableStateListOf(*initialMockSubscriptions.toTypedArray())

fun isDateOverlapping(start1: String, end1: String, start2: String, end2: String): Boolean {
    if (start1.isBlank() || end1.isBlank() || start2.isBlank() || end2.isBlank()) return false
    return start1 <= end2 && start2 <= end1
}

fun getUsedSessions(subscriptionId: Int): Int {
    return globalAttendance.count { att -> att.games.any { it.subscriptionId == subscriptionId } }
}

fun getSubscriptionStatus(sub: Subscription): String {
    val used = getUsedSessions(sub.id)
    val remaining = sub.totalSessions - used
    val today = getCurrentDate()
    return when {
        remaining <= 0 -> "منتهي"
        today < sub.startDate -> "قادم"
        today > sub.endDate -> "منتهي"
        else -> "نشط"
    }
}

@Composable
fun SubscriptionsScreen() {
    var selectedSubscription by remember { mutableStateOf<Subscription?>(null) }

    if (selectedSubscription != null) {
        SubscriptionDetailsScreen(
            subscription = selectedSubscription!!,
            onBack = { selectedSubscription = null }
        )
    } else {
        SubscriptionsListScreen(
            onSubscriptionClick = { selectedSubscription = it }
        )
    }
}

@Composable
fun SubscriptionsListScreen(onSubscriptionClick: (Subscription) -> Unit) {
    var selectedFilter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "نشط", "منتهي")
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredSubscriptions = globalSubscriptions.filter {
        when (selectedFilter) {
            "نشط" -> it.status == "نشط"
            "منتهي" -> it.status == "منتهي"
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = "إضافة اشتراك") },
                text = { Text("إضافة اشتراك") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Button(
                        onClick = { selectedFilter = filter },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(filter)
                    }
                }
            }

            if (filteredSubscriptions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Text("لم يتم العثور على اشتراكات.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredSubscriptions, key = { it.id }) { subscription ->
                        SubscriptionItem(subscription = subscription, onClick = { onSubscriptionClick(subscription) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { member, game, type, startDate, endDate, price, totalSessions ->
                val newId = (globalSubscriptions.maxOfOrNull { it.id } ?: 0) + 1
                globalSubscriptions.add(
                    Subscription(
                        id = newId,
                        memberId = member.id,
                        memberName = member.name,
                        gameId = game.id,
                        gameName = game.name,
                        planType = type,
                        startDate = startDate,
                        endDate = endDate,
                        price = price.toDoubleOrNull() ?: game.price,
                        totalSessions = totalSessions,
                        status = "نشط"
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription, onClick: () -> Unit) {
    val usedSessions = getUsedSessions(subscription.id)
    val remainingSessions = (subscription.totalSessions - usedSessions).coerceAtLeast(0)
    val computedStatus = getSubscriptionStatus(subscription)

    val statusColor = when (computedStatus) {
        "نشط" -> MaterialTheme.colorScheme.primary
        "منتهي" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subscription.memberName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "اللعبة: ${subscription.gameName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = "الاشتراك: ${subscription.planType} | السعر: $${subscription.price}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "الجلسات: الإجمالي (${subscription.totalSessions}) | المستخدمة ($usedSessions) | المتبقية ($remainingSessions)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = computedStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${subscription.startDate} إلى ${subscription.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailsScreen(
    subscription: Subscription,
    onBack: () -> Unit
) {
    val usedSessions = getUsedSessions(subscription.id)
    val remainingSessions = (subscription.totalSessions - usedSessions).coerceAtLeast(0)
    val computedStatus = getSubscriptionStatus(subscription)

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("تفاصيل الاشتراك") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("العضو: ${subscription.memberName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    Text("اللعبة: ${subscription.gameName}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("النوع: ${subscription.planType}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("السعر: $${subscription.price}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("إجمالي الجلسات: ${subscription.totalSessions}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("الجلسات المستخدمة: $usedSessions", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("الجلسات المتبقية: $remainingSessions", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("تاريخ البدء: ${subscription.startDate}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("تاريخ الانتهاء: ${subscription.endDate}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    
                    val statusColor = when (computedStatus) {
                        "نشط" -> MaterialTheme.colorScheme.primary
                        "منتهي" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                    Text(
                        text = "الحالة: $computedStatus",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSave: (member: Member, game: Game, type: String, startDate: String, endDate: String, price: String, totalSessions: Int) -> Unit
) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var gameMenuExpanded by remember { mutableStateOf(false) }

    var type by remember { mutableStateOf("شهري") }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    val types = listOf("شهري", "3 أشهر", "6 أشهر", "سنوي")

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var totalSessionsStr by remember { mutableStateOf("12") }
    var price by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة اشتراك جديد") },
        text = {
            Column {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Member Selection Dropdown
                Box {
                    OutlinedButton(
                        onClick = { memberMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(selectedMember?.name ?: "اختر العضو *")
                    }
                    DropdownMenu(
                        expanded = memberMenuExpanded,
                        onDismissRequest = { memberMenuExpanded = false }
                    ) {
                        if (globalMembers.isEmpty()) {
                            DropdownMenuItem(text = { Text("لا يوجد أعضاء") }, onClick = { memberMenuExpanded = false })
                        } else {
                            globalMembers.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        selectedMember = m
                                        memberMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Game Selection Dropdown
                Box {
                    OutlinedButton(
                        onClick = { gameMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(selectedGame?.name ?: "اختر اللعبة *")
                    }
                    DropdownMenu(
                        expanded = gameMenuExpanded,
                        onDismissRequest = { gameMenuExpanded = false }
                    ) {
                        globalGames.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                onClick = {
                                    selectedGame = g
                                    price = g.price.toString()
                                    gameMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Type Selection Dropdown
                Box {
                    OutlinedButton(
                        onClick = { typeMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("النوع: $type")
                    }
                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    type = t
                                    typeMenuExpanded = false
                                    if (t == "شهري") totalSessionsStr = "12"
                                    if (t == "3 أشهر") totalSessionsStr = "36"
                                    if (t == "6 أشهر") totalSessionsStr = "72"
                                    if (t == "سنوي") totalSessionsStr = "144"
                                }
                            )
                        }
                    }
                }

                DatePickerField(
                    value = startDate,
                    onValueChange = { startDate = it; errorMessage = null },
                    label = "تاريخ البدء *",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                DatePickerField(
                    value = endDate,
                    onValueChange = { endDate = it; errorMessage = null },
                    label = "تاريخ الانتهاء *",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = totalSessionsStr,
                    onValueChange = { totalSessionsStr = it; errorMessage = null },
                    label = { Text("عدد الجلسات الإجمالي *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; errorMessage = null },
                    label = { Text("السعر *") },
                    placeholder = { Text("$0.00") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedSessions = totalSessionsStr.toIntOrNull()
                    if (selectedMember == null || selectedGame == null || startDate.isBlank() || endDate.isBlank() || price.isBlank() || parsedSessions == null || parsedSessions <= 0) {
                        errorMessage = "جميع الحقول واختيار العضو واللعبة مطلوبة مع التأكد من القيم."
                    } else {
                        val hasOverlap = globalSubscriptions.any { existing ->
                            existing.memberId == selectedMember!!.id &&
                            existing.gameId == selectedGame!!.id &&
                            getSubscriptionStatus(existing) != "منتهي" &&
                            isDateOverlapping(startDate, endDate, existing.startDate, existing.endDate)
                        }

                        if (hasOverlap) {
                            errorMessage = "يوجد اشتراك سارٍ أو متداخل لنفس العضو واللعبة خلال هذه الفترة."
                        } else {
                            onSave(selectedMember!!, selectedGame!!, type, startDate, endDate, price, parsedSessions)
                        }
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
