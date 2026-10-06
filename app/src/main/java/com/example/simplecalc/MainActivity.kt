package com.example.simplecalc

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val locale = Locale("ar")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        
        setContent {
            val config = Configuration(LocalConfiguration.current)
            config.setLocale(locale)
            
            SimpleCalcTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalConfiguration provides config
                ) {
                    GymManagementApp()
                }
            }
        }
    }
}

enum class GymDestination(val title: String, val icon: ImageVector) {
    Dashboard("لوحة التحكم", Icons.Default.Home),
    Members("الأعضاء", Icons.Default.Person),
    Subscriptions("الاشتراكات", Icons.AutoMirrored.Filled.List),
    Attendance("الحضور", Icons.Default.CheckCircle),
    Admin("الإدارة", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymManagementApp() {
    var currentDestination by rememberSaveable { mutableStateOf(GymDestination.Dashboard) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = currentDestination.title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                GymDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            when (currentDestination) {
                GymDestination.Dashboard -> DashboardScreen()
                GymDestination.Members -> MembersScreen()
                GymDestination.Subscriptions -> SubscriptionsScreen()
                GymDestination.Attendance -> AttendanceScreen()
                GymDestination.Admin -> AdminScreen()
            }
        }
    }
}

fun isExpiringSoon(endDate: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val end = sdf.parse(endDate) ?: return false
        val diff = end.time - Date().time
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        days in 0..7
    } catch (e: Exception) { false }
}

@Composable
fun DashboardScreen() {
    var showAddMember by remember { mutableStateOf(false) }
    var showAddSubscription by remember { mutableStateOf(false) }
    var showAddPayment by remember { mutableStateOf(false) }
    var showCheckIn by remember { mutableStateOf(false) }

    val todayDate = getCurrentDate()
    val totalMembers = globalMembers.size
    val activeMembers = globalMembers.count { it.status == "نشط" }
    
    val todayRecords = globalAttendance.filter { it.date == todayDate }
    val todayCheckIns = todayRecords.size
    val currentlyInside = todayRecords.count { it.status == "حاضر" }
    val sessionsUsedToday = todayRecords.sumOf { it.games.size }
    
    val todayPay = globalPayments.filter { it.date == todayDate }.sumOf { it.amount }
    
    val expSoon = globalSubscriptions.count { it.status == "نشط" && isExpiringSoon(it.endDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardWidget(title = "إجمالي الأعضاء", value = totalMembers.toString(), modifier = Modifier.weight(1f))
            DashboardWidget(title = "الأعضاء النشطين", value = activeMembers.toString(), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardWidget(title = "حضور اليوم", value = todayCheckIns.toString(), modifier = Modifier.weight(1f))
            DashboardWidget(title = "بالداخل حالياً", value = currentlyInside.toString(), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardWidget(title = "جلسات مستخدمة اليوم", value = sessionsUsedToday.toString(), modifier = Modifier.weight(1f))
            DashboardWidget(title = "اشتراكات تنتهي قريباً", value = expSoon.toString(), modifier = Modifier.weight(1f), colorOverride = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))
        DashboardWidget(title = "مدفوعات اليوم", value = "$$todayPay", modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
        Text("إجراءات سريعة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            QuickActionButton(icon = Icons.Default.Person, label = "عضو") { showAddMember = true }
            QuickActionButton(icon = Icons.AutoMirrored.Filled.List, label = "اشتراك") { showAddSubscription = true }
            QuickActionButton(icon = Icons.Default.ShoppingCart, label = "دفع") { showAddPayment = true }
            QuickActionButton(icon = Icons.Default.CheckCircle, label = "حضور") { showCheckIn = true }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("النشاط الأخير", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        val recentActivities = listOfNotNull(
            globalMembers.lastOrNull()?.let { "عضو جديد: ${it.name}" },
            globalSubscriptions.lastOrNull()?.let { "اشتراك جديد: ${it.gameName} لـ ${it.memberName}" },
            globalPayments.lastOrNull()?.let { "دفعة: $${it.amount} من ${it.memberName}" },
            globalAttendance.lastOrNull()?.let { "تسجيل حضور: ${it.memberName} في ${it.checkInTime}" }
        )
        
        if (recentActivities.isEmpty()) {
            Text("لا يوجد نشاط أخير.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            recentActivities.forEach { activity ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(text = activity, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Dialogs
    if (showAddMember) {
        AddMemberDialog(
            onDismiss = { showAddMember = false },
            onSave = { name, phone, gender, dob, joinDate ->
                val newId = (globalMembers.maxOfOrNull { it.id } ?: 0) + 1
                globalMembers.add(
                    Member(newId, name, phone, gender, dob, joinDate, "نشط", "2025-12-31")
                )
                showAddMember = false
            }
        )
    }
    if (showAddSubscription) {
        AddSubscriptionDialog(
            onDismiss = { showAddSubscription = false },
            onSave = { member, game, type, startDate, endDate, price, totalSessions ->
                val newId = (globalSubscriptions.maxOfOrNull { it.id } ?: 0) + 1
                globalSubscriptions.add(
                    Subscription(newId, member.id, member.name, game.id, game.name, type, startDate, endDate, price.toDoubleOrNull() ?: game.price, totalSessions, "نشط")
                )
                showAddSubscription = false
            }
        )
    }
    if (showAddPayment) {
        AddPaymentDialog(
            onDismiss = { showAddPayment = false },
            onSave = { member, subscription, amount, date, method, notes ->
                val newId = (globalPayments.maxOfOrNull { it.id } ?: 0) + 1
                globalPayments.add(
                    Payment(newId, member.id, member.name, subscription.id, subscription.gameName, amount, date, method, notes)
                )
                showAddPayment = false
            }
        )
    }
    if (showCheckIn) {
        CheckInDialog(
            onDismiss = { showCheckIn = false },
            onSaveNew = { member, weight, notes, games ->
                val newId = (globalAttendance.maxOfOrNull { it.id } ?: 0) + 1
                globalAttendance.add(
                    Attendance(newId, member.id, member.name, getCurrentTime(), null, "حاضر", getCurrentDate(), games, weight, notes)
                )
                if (weight != null) {
                    val newMeasId = (globalMeasurements.maxOfOrNull { it.id } ?: 0) + 1
                    globalMeasurements.add(MemberMeasurement(newMeasId, member.id, getCurrentDate(), weight, notes))
                }
                showCheckIn = false
            },
            onCheckOutExisting = { existingRecord ->
                val index = globalAttendance.indexOfFirst { it.id == existingRecord.id }
                if (index != -1) {
                    globalAttendance[index] = existingRecord.copy(
                        checkOutTime = getCurrentTime(),
                        status = "منصرف"
                    )
                }
                showCheckIn = false
            }
        )
    }
}

@Composable
fun DashboardWidget(title: String, value: String, modifier: Modifier = Modifier, colorOverride: Color? = null) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = colorOverride ?: MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    if (isPressed) {
        showDatePicker = true
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onValueChange(sdf.format(Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("موافق")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("إلغاء")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "اختر التاريخ",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        isError = isError,
        interactionSource = interactionSource,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "اختر التاريخ")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    SimpleCalcTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            GymManagementApp()
        }
    }
}
