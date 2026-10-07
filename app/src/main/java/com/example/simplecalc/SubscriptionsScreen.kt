package com.example.simplecalc

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.PlanType
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.repository.AddSubscriptionResult
import com.example.simplecalc.ui.AppViewModelProvider
import com.example.simplecalc.ui.components.AppCard
import com.example.simplecalc.ui.components.EmptyState
import com.example.simplecalc.ui.components.MemberAvatar
import com.example.simplecalc.ui.components.SectionHeader
import com.example.simplecalc.ui.components.StatusChip
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import com.example.simplecalc.ui.viewmodel.SubscriptionsUiState
import com.example.simplecalc.ui.viewmodel.SubscriptionsViewModel
import java.util.concurrent.TimeUnit

@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedSubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }

    if (selectedSubscription != null) {
        SubscriptionDetailsScreen(
            subscription = selectedSubscription!!,
            onBack = { selectedSubscription = null }
        )
    } else {
        SubscriptionsListScreen(
            uiState = uiState,
            viewModel = viewModel,
            onSubscriptionClick = { selectedSubscription = it }
        )
    }
}

@Composable
fun SubscriptionsListScreen(
    uiState: SubscriptionsUiState,
    viewModel: SubscriptionsViewModel,
    onSubscriptionClick: (SubscriptionEntity) -> Unit
) {
    val filters = listOf("الكل", "نشط", "منتهي", "قادم")
    var showAddDialog by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterSelected(filter) },
                        label = { Text(filter) }
                    )
                }
            }

            if (uiState.subscriptions.isEmpty()) {
                EmptyState(icon = Icons.AutoMirrored.Filled.List, title = "لا يوجد نتائج", subtitle = "لم يتم العثور على اشتراكات.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.subscriptions, key = { it.id }) { subscription ->
                        SubscriptionItem(
                            subscription = subscription,
                            members = uiState.members,
                            games = uiState.games,
                            onClick = { onSubscriptionClick(subscription) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            members = uiState.members,
            games = uiState.games,
            onDismiss = { showAddDialog = false },
            viewModel = viewModel,
            onSuccess = { showAddDialog = false }
        )
    }
}

@Composable
fun SubscriptionItem(
    subscription: SubscriptionEntity,
    members: List<MemberEntity>,
    games: List<GameEntity>,
    onClick: () -> Unit
) {
    val member = members.find { it.id == subscription.memberId }
    val game = games.find { it.id == subscription.gameId }
    val memberName = member?.name ?: "عضو #${subscription.memberId}"
    val gameName = game?.name ?: "لعبة #${subscription.gameId}"

    val todayEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    val computedStatus = when {
        subscription.startDate > todayEpoch -> "قادم"
        subscription.endDate < todayEpoch -> "منتهي"
        else -> "نشط"
    }

    AppCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                MemberAvatar(name = memberName)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = memberName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "اللعبة: $gameName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                StatusChip(text = computedStatus, status = computedStatus)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "الخطة: ${subscription.planType.name} | السعر: $${subscription.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatEpochDay(subscription.startDate)} إلى ${formatEpochDay(subscription.endDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailsScreen(
    subscription: SubscriptionEntity,
    onBack: () -> Unit
) {
    val todayEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    val computedStatus = when {
        subscription.startDate > todayEpoch -> "قادم"
        subscription.endDate < todayEpoch -> "منتهي"
        else -> "نشط"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SectionHeader("معلومات الاشتراك")
            AppCard {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("اشتراك #${subscription.id}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        StatusChip(text = computedStatus, status = computedStatus)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("الخطة: ${subscription.planType.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                    Text("السعر: $${subscription.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                    Text("تاريخ البدء: ${formatEpochDay(subscription.startDate)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                    Text("تاريخ الانتهاء: ${formatEpochDay(subscription.endDate)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    members: List<MemberEntity>,
    games: List<GameEntity>,
    onDismiss: () -> Unit,
    viewModel: SubscriptionsViewModel,
    onSuccess: () -> Unit
) {
    var selectedMember by remember { mutableStateOf<MemberEntity?>(null) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    var selectedGame by remember { mutableStateOf<GameEntity?>(null) }
    var gameMenuExpanded by remember { mutableStateOf(false) }

    var selectedPlanType by remember { mutableStateOf(PlanType.MONTHLY) }
    var priceStr by remember { mutableStateOf("") }
    
    val todayEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    var startDateStr by remember { mutableStateOf(formatEpochDay(todayEpoch)) }
    var endDateStr by remember { mutableStateOf(formatEpochDay(todayEpoch + 30)) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.addSubscriptionResult) {
        viewModel.addSubscriptionResult.collect { result ->
            when (result) {
                is AddSubscriptionResult.Success -> onSuccess()
                is AddSubscriptionResult.OverlapError -> {
                    errorMessage = "${result.message}\nتاريخ البدء المقترح: ${formatEpochDay(result.suggestedStartDateEpochDay)}"
                    startDateStr = formatEpochDay(result.suggestedStartDateEpochDay)
                    endDateStr = formatEpochDay(result.suggestedStartDateEpochDay + 30)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة اشتراك جديد") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Member Dropdown
                Box {
                    OutlinedButton(
                        onClick = { memberMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(selectedMember?.name ?: "اختر العضو *")
                    }
                    DropdownMenu(
                        expanded = memberMenuExpanded,
                        onDismissRequest = { memberMenuExpanded = false }
                    ) {
                        if (members.isEmpty()) {
                            DropdownMenuItem(text = { Text("لا يوجد أعضاء") }, onClick = { memberMenuExpanded = false })
                        } else {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        selectedMember = m
                                        memberMenuExpanded = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                // Game Dropdown
                Box {
                    OutlinedButton(
                        onClick = { gameMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(selectedGame?.name ?: "اختر اللعبة *")
                    }
                    DropdownMenu(
                        expanded = gameMenuExpanded,
                        onDismissRequest = { gameMenuExpanded = false }
                    ) {
                        if (games.isEmpty()) {
                            DropdownMenuItem(text = { Text("لا يوجد ألعاب") }, onClick = { gameMenuExpanded = false })
                        } else {
                            games.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.name) },
                                    onClick = {
                                        selectedGame = g
                                        priceStr = g.defaultPrice.toString()
                                        gameMenuExpanded = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                // Plan Type Selection
                Text("نوع الخطة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PlanType.entries.forEach { p ->
                        val isSelected = selectedPlanType == p
                        val label = when (p) {
                            PlanType.MONTHLY -> "شهري"
                            PlanType.QUARTERLY -> "3 شهور"
                            PlanType.YEARLY -> "سنوي"
                        }
                        OutlinedButton(
                            onClick = {
                                selectedPlanType = p
                                val startEp = parseEpochDay(startDateStr)
                                val days = when (p) {
                                    PlanType.MONTHLY -> 30L
                                    PlanType.QUARTERLY -> 90L
                                    PlanType.YEARLY -> 365L
                                }
                                endDateStr = formatEpochDay(startEp + days)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("السعر *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                DatePickerField(
                    value = startDateStr,
                    onValueChange = {
                        startDateStr = it
                        val startEp = parseEpochDay(it)
                        val days = when (selectedPlanType) {
                            PlanType.MONTHLY -> 30L
                            PlanType.QUARTERLY -> 90L
                            PlanType.YEARLY -> 365L
                        }
                        endDateStr = formatEpochDay(startEp + days)
                    },
                    label = "تاريخ البدء *",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                DatePickerField(
                    value = endDateStr,
                    onValueChange = { endDateStr = it },
                    label = "تاريخ الانتهاء *",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = selectedMember
                    val g = selectedGame
                    val price = priceStr.toDoubleOrNull()
                    if (m == null || g == null || price == null) {
                        errorMessage = "يرجى تعبئة جميع الحقول المطلوبة بشكل صحيح."
                    } else {
                        viewModel.addSubscription(
                            memberId = m.id,
                            gameId = g.id,
                            planType = selectedPlanType,
                            price = price,
                            startDateEpochDay = parseEpochDay(startDateStr),
                            endDateEpochDay = parseEpochDay(endDateStr)
                        )
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

@Preview(showBackground = true)
@Composable
fun SubscriptionsScreenPreview() {
    SimpleCalcTheme {
        SubscriptionsScreen()
    }
}