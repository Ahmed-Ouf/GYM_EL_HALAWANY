package com.example.simplecalc

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
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
import com.example.simplecalc.ui.components.AppCard
import com.example.simplecalc.ui.components.EmptyState
import com.example.simplecalc.ui.components.SectionHeader
import com.example.simplecalc.ui.components.StatCard
import com.example.simplecalc.ui.components.StatusChip
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import com.example.simplecalc.ui.theme.StatusColorsDark
import com.example.simplecalc.ui.theme.StatusColorsLight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplecalc.ui.AppViewModelProvider
import com.example.simplecalc.ui.viewmodel.AttendanceViewModel
import com.example.simplecalc.ui.viewmodel.DashboardViewModel
import com.example.simplecalc.ui.viewmodel.MembersViewModel
import com.example.simplecalc.ui.viewmodel.PaymentsViewModel
import com.example.simplecalc.ui.viewmodel.SubscriptionsViewModel
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
        
        setContent {
            SimpleCalcTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl
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
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { it / 8 }).togetherWith(fadeOut() + slideOutHorizontally { -it / 8 })
                },
                label = "ScreenTransition"
            ) { destination ->
                when (destination) {
                    GymDestination.Dashboard -> DashboardScreen()
                    GymDestination.Members -> MembersScreen()
                    GymDestination.Subscriptions -> SubscriptionsScreen()
                    GymDestination.Attendance -> AttendanceScreen()
                    GymDestination.Admin -> AdminScreen()
                }
            }
        }
    }
}

fun isExpiringSoon(endDate: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val end = sdf.parse(endDate) ?: return false
        val diff = end.time - Date().time
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        days in 0..7
    } catch (e: Exception) { false }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddMember by remember { mutableStateOf(false) }
    var showAddSubscription by remember { mutableStateOf(false) }
    var showAddPayment by remember { mutableStateOf(false) }
    var showCheckIn by remember { mutableStateOf(false) }

    val membersViewModel: MembersViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val subsViewModel: SubscriptionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val paymentsViewModel: PaymentsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val attendanceViewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val subsUiState by subsViewModel.uiState.collectAsStateWithLifecycle()
    val paymentsUiState by paymentsViewModel.uiState.collectAsStateWithLifecycle()
    val attendanceUiState by attendanceViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader("لوحة التحكم")

        // 2x2 Grid of StatCards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(icon = Icons.Default.Person, value = "${uiState.totalMembers}", label = "إجمالي الأعضاء", accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            StatCard(icon = Icons.Default.CheckCircle, value = "${uiState.activeMembers}", label = "أعضاء نشطين", accentColor = StatusColorsLight.Active, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(icon = Icons.Default.Home, value = "${uiState.todayCheckIns}", label = "حضور اليوم", accentColor = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
            StatCard(icon = Icons.Default.ShoppingCart, value = "$${uiState.todayPay}", label = "مدفوعات اليوم", accentColor = StatusColorsLight.Warning, modifier = Modifier.weight(1f))
        }

        if (uiState.expSoonCount > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusColorsLight.Warning, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "تنتهي صلاحية ${uiState.expSoonCount} اشتراك(ات) خلال 7 أيام قادمة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("إجراءات سريعة")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Box(modifier = Modifier.weight(1f)) { QuickActionSquare(icon = Icons.Default.Person, label = "عضو") { showAddMember = true } }
            Box(modifier = Modifier.weight(1f)) { QuickActionSquare(icon = Icons.AutoMirrored.Filled.List, label = "اشتراك") { showAddSubscription = true } }
            Box(modifier = Modifier.weight(1f)) { QuickActionSquare(icon = Icons.Default.ShoppingCart, label = "دفع") { showAddPayment = true } }
            Box(modifier = Modifier.weight(1f)) { QuickActionSquare(icon = Icons.Default.CheckCircle, label = "حضور") { showCheckIn = true } }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("حالة الاشتراكات")
        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val isDark = isSystemInDarkTheme()
                StatusItem(
                    "نشط", uiState.activeSubsCount,
                    color = if (isDark) StatusColorsDark.Active else StatusColorsLight.Active,
                    backgroundColor = if (isDark) StatusColorsDark.ActiveContainer else StatusColorsLight.ActiveContainer,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                StatusItem(
                    "قادم", uiState.upcomingSubsCount,
                    color = if (isDark) StatusColorsDark.Warning else StatusColorsLight.Warning,
                    backgroundColor = if (isDark) StatusColorsDark.WarningContainer else StatusColorsLight.WarningContainer,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                StatusItem(
                    "منتهي", uiState.expiredSubsCount,
                    color = if (isDark) StatusColorsDark.Expired else StatusColorsLight.Expired,
                    backgroundColor = if (isDark) StatusColorsDark.ExpiredContainer else StatusColorsLight.ExpiredContainer,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("النشاط الأخير")
        if (uiState.recentActivities.isEmpty()) {
            EmptyState(icon = Icons.Default.Notifications, title = "لا يوجد نشاط", subtitle = "لم يتم تسجيل أي أحداث مؤخراً.")
        } else {
            uiState.recentActivities.forEach { text ->
                AppCard(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = text, style = MaterialTheme.typography.bodyMedium)
                    }
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
                val birthEpoch = if (dob.isBlank()) null else parseEpochDay(dob)
                val joinEpoch = if (joinDate.isBlank()) TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) else parseEpochDay(joinDate)
                membersViewModel.addMember(name, phone, gender, birthEpoch, joinEpoch)
                showAddMember = false
            }
        )
    }
    if (showAddSubscription) {
        AddSubscriptionDialog(
            members = subsUiState.members,
            games = subsUiState.games,
            onDismiss = { showAddSubscription = false },
            viewModel = subsViewModel,
            onSuccess = { showAddSubscription = false }
        )
    }
    if (showAddPayment) {
        AddPaymentDialog(
            members = paymentsUiState.members,
            subscriptions = paymentsUiState.subscriptions,
            onDismiss = { showAddPayment = false },
            onSave = { memberId, subscriptionId, amount, method, date, notes ->
                paymentsViewModel.recordPayment(memberId, subscriptionId, amount, method, parseEpochDay(date), notes)
                showAddPayment = false
            }
        )
    }
    if (showCheckIn) {
        CheckInDialog(
            members = attendanceUiState.members,
            onDismiss = { showCheckIn = false },
            viewModel = attendanceViewModel,
            onSuccess = { showCheckIn = false }
        )
    }
}



@Composable
fun StatusItem(label: String, count: Int, color: Color, backgroundColor: Color = color.copy(alpha = 0.12f), modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text("$count", style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun QuickActionSquare(icon: ImageVector, label: String, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.padding(4.dp),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
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
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
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
        value = formatDisplayDate(value),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        isError = isError,
        shape = MaterialTheme.shapes.medium,
        interactionSource = interactionSource,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "اختر التاريخ")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

fun formatDisplayDate(dateStr: String): String {
    if (dateStr.isBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(dateStr) ?: return dateStr
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        "\u200E" + formatter.format(date)
    } catch (e: Exception) {
        "\u200E$dateStr"
    }
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
