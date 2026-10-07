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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.PaymentEntity
import com.example.simplecalc.data.local.entity.PaymentMethod
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.ui.AppViewModelProvider
import com.example.simplecalc.ui.components.AppCard
import com.example.simplecalc.ui.components.EmptyState
import com.example.simplecalc.ui.components.MemberAvatar
import com.example.simplecalc.ui.components.SectionHeader
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import com.example.simplecalc.ui.viewmodel.PaymentsUiState
import com.example.simplecalc.ui.viewmodel.PaymentsViewModel
import java.util.concurrent.TimeUnit

@Composable
fun PaymentsScreen(
    onBack: (() -> Unit)? = null,
    viewModel: PaymentsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPayment by remember { mutableStateOf<PaymentEntity?>(null) }

    if (selectedPayment != null) {
        PaymentDetailsScreen(
            payment = selectedPayment!!,
            members = uiState.members,
            subscriptions = uiState.subscriptions,
            onBack = { selectedPayment = null }
        )
    } else {
        PaymentsListScreen(
            uiState = uiState,
            viewModel = viewModel,
            onPaymentClick = { selectedPayment = it }
        )
    }
}

@Composable
fun PaymentsListScreen(
    uiState: PaymentsUiState,
    viewModel: PaymentsViewModel,
    onPaymentClick: (PaymentEntity) -> Unit
) {
    val filters = listOf("الكل", "نقدي", "بطاقة", "تحويل بنكي")
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = "تسجيل دفعة") },
                text = { Text("تسجيل دفعة") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي المدفوعات", style = MaterialTheme.typography.bodyMedium)
                        Text("$${uiState.totalAmount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("العدد", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.paymentCount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filters
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

            // List
            if (uiState.payments.isEmpty()) {
                EmptyState(icon = Icons.Default.ShoppingCart, title = "لا يوجد مدفوعات", subtitle = "لم يتم تسجيل أي مدفوعات بعد.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    items(uiState.payments, key = { it.id }) { payment ->
                        PaymentItem(
                            payment = payment,
                            members = uiState.members,
                            subscriptions = uiState.subscriptions,
                            onClick = { onPaymentClick(payment) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPaymentDialog(
            members = uiState.members,
            subscriptions = uiState.subscriptions,
            onDismiss = { showAddDialog = false },
            onSave = { memberId, subscriptionId, amount, method, date, notes ->
                viewModel.recordPayment(memberId, subscriptionId, amount, method, parseEpochDay(date), notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PaymentItem(
    payment: PaymentEntity,
    members: List<MemberEntity>,
    subscriptions: List<SubscriptionEntity>,
    onClick: () -> Unit
) {
    val member = members.find { it.id == payment.memberId }
    val memberName = member?.name ?: "عضو #${payment.memberId}"

    val methodLabel = when (payment.method) {
        PaymentMethod.CASH -> "نقدي"
        PaymentMethod.CARD -> "بطاقة"
        PaymentMethod.TRANSFER -> "تحويل بنكي"
    }

    AppCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(name = memberName)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = memberName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "$${payment.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "الطريقة: $methodLabel | التاريخ: ${formatEpochDay(payment.date)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!payment.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ملاحظات: ${payment.note}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailsScreen(
    payment: PaymentEntity,
    members: List<MemberEntity>,
    subscriptions: List<SubscriptionEntity>,
    onBack: () -> Unit
) {
    val member = members.find { it.id == payment.memberId }
    val memberName = member?.name ?: "عضو #${payment.memberId}"

    val methodLabel = when (payment.method) {
        PaymentMethod.CASH -> "نقدي"
        PaymentMethod.CARD -> "بطاقة"
        PaymentMethod.TRANSFER -> "تحويل بنكي"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("تفاصيل الدفع") },
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
            SectionHeader("معلومات الدفعة")
            AppCard {
                Column {
                    Text("العضو: $memberName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    Text("المبلغ: $${payment.amount}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                    Text("التاريخ: ${formatEpochDay(payment.date)}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("طريقة الدفع: $methodLabel", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    if (!payment.note.isNullOrBlank()) {
                        Text("ملاحظات: ${payment.note}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    members: List<MemberEntity>,
    subscriptions: List<SubscriptionEntity>,
    onDismiss: () -> Unit,
    onSave: (memberId: Long, subscriptionId: Long, amount: Double, method: PaymentMethod, date: String, notes: String?) -> Unit
) {
    var selectedMember by remember { mutableStateOf<MemberEntity?>(null) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    var selectedSubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var subscriptionMenuExpanded by remember { mutableStateOf(false) }

    var amountStr by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var date by remember { mutableStateOf(formatEpochDay(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()))) }
    var notes by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val memberSubscriptions = subscriptions.filter { it.memberId == selectedMember?.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل دفعة جديدة") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (showError) {
                    Text(
                        text = "يرجى اختيار العضو والاشتراك وإدخال مبلغ صحيح.",
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
                                        selectedSubscription = null
                                        memberMenuExpanded = false
                                        showError = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Subscription Dropdown
                Box {
                    OutlinedButton(
                        onClick = { subscriptionMenuExpanded = true },
                        enabled = selectedMember != null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(selectedSubscription?.let { "اشتراك #${it.id} ($${it.price})" } ?: "اختر الاشتراك *")
                    }
                    DropdownMenu(
                        expanded = subscriptionMenuExpanded,
                        onDismissRequest = { subscriptionMenuExpanded = false }
                    ) {
                        if (memberSubscriptions.isEmpty()) {
                            DropdownMenuItem(text = { Text("لا يوجد اشتراكات لهذا العضو") }, onClick = { subscriptionMenuExpanded = false })
                        } else {
                            memberSubscriptions.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("اشتراك #${s.id} - ${s.planType.name} - $${s.price}") },
                                    onClick = {
                                        selectedSubscription = s
                                        amountStr = s.price.toString()
                                        subscriptionMenuExpanded = false
                                        showError = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it; showError = false },
                    label = { Text("المبلغ *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Text("طريقة الدفع", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PaymentMethod.entries.forEach { m ->
                        val isSelected = selectedMethod == m
                        val label = when (m) {
                            PaymentMethod.CASH -> "نقدي"
                            PaymentMethod.CARD -> "بطاقة"
                            PaymentMethod.TRANSFER -> "تحويل بنكي"
                        }
                        OutlinedButton(
                            onClick = { selectedMethod = m },
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

                DatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    label = "تاريخ الدفع *",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = selectedMember
                    val s = selectedSubscription
                    val parsedAmount = amountStr.toDoubleOrNull()
                    if (m == null || s == null || parsedAmount == null || parsedAmount <= 0) {
                        showError = true
                    } else {
                        onSave(m.id, s.id, parsedAmount, selectedMethod, date, notes.ifBlank { null })
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
fun PaymentsScreenPreview() {
    SimpleCalcTheme {
        PaymentsScreen()
    }
}