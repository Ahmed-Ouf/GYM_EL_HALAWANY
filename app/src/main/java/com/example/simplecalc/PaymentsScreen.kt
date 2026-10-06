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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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

data class Payment(
    val id: Int,
    val memberId: Int,
    val memberName: String,
    val subscriptionId: Int,
    val subscriptionDetails: String,
    val amount: Double,
    val date: String,
    val method: String,
    val notes: String
)

val initialMockPayments = listOf(
    Payment(1, 1, "أحمد محمد", 1, "رفع الأثقال - شهري", 500.0, "2024-10-01", "بطاقة", "تم الدفع في الاستقبال"),
    Payment(2, 2, "سارة أحمد", 2, "سباحة - سنوي", 400.0, "2024-01-01", "تحويل بنكي", ""),
    Payment(3, 3, "خالد محمود", 3, "ملاكمة - 3 أشهر", 1800.0, "2024-12-01", "نقدي", "")
)

val globalPayments = mutableStateListOf(*initialMockPayments.toTypedArray())

@Composable
fun PaymentsScreen(onBack: (() -> Unit)? = null) {
    var selectedPayment by remember { mutableStateOf<Payment?>(null) }

    if (selectedPayment != null) {
        PaymentDetailsScreen(
            payment = selectedPayment!!,
            onBack = { selectedPayment = null }
        )
    } else {
        PaymentsListScreen(
            onPaymentClick = { selectedPayment = it },
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsListScreen(onPaymentClick: (Payment) -> Unit, onBack: (() -> Unit)? = null) {
    var selectedFilter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "نقدي", "بطاقة", "تحويل بنكي")
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredPayments = globalPayments.filter {
        when (selectedFilter) {
            "الكل" -> true
            else -> it.method == selectedFilter
        }
    }

    val totalAmount = filteredPayments.sumOf { it.amount }
    val paymentCount = filteredPayments.size

    Scaffold(
        topBar = {
            if (onBack != null) {
                CenterAlignedTopAppBar(
                    title = { Text("المدفوعات", fontWeight = FontWeight.Bold) },
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
        },
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
                    .padding(16.dp),
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
                        Text("$$totalAmount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("العدد", style = MaterialTheme.typography.bodyMedium)
                        Text("$paymentCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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

            // List
            if (filteredPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Text("لم يتم العثور على مدفوعات.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    items(filteredPayments, key = { it.id }) { payment ->
                        PaymentItem(payment = payment, onClick = { onPaymentClick(payment) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPaymentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { member, subscription, amount, date, method, notes ->
                val newId = (globalPayments.maxOfOrNull { it.id } ?: 0) + 1
                globalPayments.add(
                    Payment(
                        id = newId,
                        memberId = member.id,
                        memberName = member.name,
                        subscriptionId = subscription.id,
                        subscriptionDetails = "${subscription.gameName} - ${subscription.planType}",
                        amount = amount,
                        date = date,
                        method = method,
                        notes = notes
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PaymentItem(payment: Payment, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = payment.memberName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${payment.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "التاريخ: ${payment.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "الطريقة: ${payment.method}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الاشتراك: ${payment.subscriptionDetails}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailsScreen(
    payment: Payment,
    onBack: () -> Unit
) {
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("العضو: ${payment.memberName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    Text("المبلغ: $${payment.amount}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Text("التاريخ: ${payment.date}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("الطريقة: ${payment.method}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("الاشتراك: ${payment.subscriptionDetails}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    
                    if (payment.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ملاحظات:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(payment.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onSave: (member: Member, subscription: Subscription, amount: Double, date: String, method: String, notes: String) -> Unit
) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    var selectedSubscription by remember { mutableStateOf<Subscription?>(null) }
    var subscriptionMenuExpanded by remember { mutableStateOf(false) }

    var method by remember { mutableStateOf("نقدي") }
    var methodMenuExpanded by remember { mutableStateOf(false) }
    val methods = listOf("نقدي", "بطاقة", "تحويل بنكي")

    var amountString by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل دفعة جديدة") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (showError) {
                    Text(
                        text = "العضو والاشتراك والمبلغ والتاريخ مطلوبة.",
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
                                        selectedSubscription = null // Reset subscription when member changes
                                        memberMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Subscription Selection Dropdown
                Box {
                    OutlinedButton(
                        onClick = { subscriptionMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        enabled = selectedMember != null
                    ) {
                        Text(selectedSubscription?.planType ?: "اختر الاشتراك *")
                    }
                    DropdownMenu(
                        expanded = subscriptionMenuExpanded,
                        onDismissRequest = { subscriptionMenuExpanded = false }
                    ) {
                        val memberSubscriptions = globalSubscriptions.filter { it.memberId == selectedMember?.id }
                        if (memberSubscriptions.isEmpty()) {
                            DropdownMenuItem(text = { Text("لم يتم العثور على اشتراكات") }, onClick = { subscriptionMenuExpanded = false })
                        } else {
                            memberSubscriptions.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.gameName} - ${s.planType} - $${s.price}") },
                                    onClick = {
                                        selectedSubscription = s
                                        subscriptionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountString,
                    onValueChange = { amountString = it; showError = false },
                    label = { Text("المبلغ *") },
                    placeholder = { Text("50.00") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                DatePickerField(
                    value = date,
                    onValueChange = { date = it; showError = false },
                    label = "تاريخ الدفع *",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                // Method Selection Dropdown
                Box {
                    OutlinedButton(
                        onClick = { methodMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("الطريقة: $method")
                    }
                    DropdownMenu(
                        expanded = methodMenuExpanded,
                        onDismissRequest = { methodMenuExpanded = false }
                    ) {
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    method = m
                                    methodMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات (اختياري)") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedAmount = amountString.toDoubleOrNull()
                    if (selectedMember == null || selectedSubscription == null || parsedAmount == null || date.isBlank()) {
                        showError = true
                    } else {
                        onSave(selectedMember!!, selectedSubscription!!, parsedAmount, date, method, notes)
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
