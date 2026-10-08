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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.MemberMeasurementEntity
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.local.entity.TrainingScheduleEntity
import com.example.simplecalc.ui.AppViewModelProvider
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import com.example.simplecalc.ui.components.AppCard
import com.example.simplecalc.ui.components.EmptyState
import com.example.simplecalc.ui.components.MemberAvatar
import com.example.simplecalc.ui.components.SectionHeader
import com.example.simplecalc.ui.components.StatusChip
import com.example.simplecalc.ui.components.appTextFieldColors
import com.example.simplecalc.ui.viewmodel.MembersUiState
import com.example.simplecalc.ui.viewmodel.MembersViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatEpochDay(epochDay: Long?): String {
    if (epochDay == null) return ""
    return try {
        val date = LocalDate.ofEpochDay(epochDay)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        "\u200E" + date.format(formatter)
    } catch (e: Exception) {
        ""
    }
}

fun parseEpochDay(dateStr: String): Long {
    return try {
        LocalDate.parse(dateStr).toEpochDay()
    } catch (e: Exception) {
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    }
}

fun getArabicDayOfWeek(offset: Int = 0): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, offset)
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "الأحد"
        Calendar.MONDAY -> "الإثنين"
        Calendar.TUESDAY -> "الثلاثاء"
        Calendar.WEDNESDAY -> "الأربعاء"
        Calendar.THURSDAY -> "الخميس"
        Calendar.FRIDAY -> "الجمعة"
        Calendar.SATURDAY -> "السبت"
        else -> ""
    }
}
val daysOfWeekArabic = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")

@Composable
fun MembersScreen(
    viewModel: MembersViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var showScheduleScreen by remember { mutableStateOf(false) }

    val selectedMember = uiState.members.find { it.id == selectedMemberId }

    if (showScheduleScreen && selectedMember != null) {
        TrainingScheduleScreen(
            member = selectedMember,
            viewModel = viewModel,
            onBack = { showScheduleScreen = false }
        )
    } else if (selectedMember != null) {
        MemberDetailsScreen(
            member = selectedMember,
            viewModel = viewModel,
            onBack = { selectedMemberId = null },
            onEdit = { updatedMember ->
                viewModel.updateMember(updatedMember)
            },
            onDelete = {
                viewModel.archiveMember(selectedMember.id)
                selectedMemberId = null
            },
            onShowSchedule = { showScheduleScreen = true }
        )
    } else {
        MembersListScreen(
            uiState = uiState,
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onAddMember = { name, phone, gender, dob, joinDate ->
                val birthEpochDay = if (dob.isBlank()) null else parseEpochDay(dob)
                val joinEpochDay = if (joinDate.isBlank()) TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) else parseEpochDay(joinDate)
                viewModel.addMember(name, phone, gender, birthEpochDay, joinEpochDay)
            },
            onMemberClick = { member -> selectedMemberId = member.id }
        )
    }
}

@Composable
fun MembersListScreen(
    uiState: MembersUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddMember: (name: String, phone: String, gender: String, dob: String, joinDate: String) -> Unit,
    onMemberClick: (MemberEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = "إضافة عضو") },
                text = { Text("إضافة عضو") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("البحث بالاسم أو رقم الهاتف") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = appTextFieldColors()
            )

            if (uiState.members.isEmpty()) {
                EmptyState(icon = Icons.Default.Search, title = "لا يوجد نتائج", subtitle = "لم يتم العثور على أعضاء.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.members, key = { it.id }) { member ->
                        MemberItem(member = member, onClick = { onMemberClick(member) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, gender, dob, joinDate ->
                onAddMember(name, phone, gender, dob, joinDate)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MemberItem(member: MemberEntity, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MemberAvatar(name = member.name)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "الهاتف: ${member.phone}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(text = "نشط", status = "نشط")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailsScreen(
    member: MemberEntity,
    viewModel: MembersViewModel,
    onBack: () -> Unit,
    onEdit: (MemberEntity) -> Unit,
    onDelete: () -> Unit,
    onShowSchedule: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val subscriptions by viewModel.getMemberSubscriptionsFlow(member.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val schedules by viewModel.getMemberSchedulesFlow(member.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val games by viewModel.getAllActiveGamesFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    val todayCal = Calendar.getInstance()
    val todayDayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val tomorrowDayOfWeek = if (todayDayOfWeek == 7) 1 else todayDayOfWeek + 1
    
    val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    val activeGameIds = subscriptions.filter { it.startDate <= todayEpochDay && it.endDate >= todayEpochDay }.map { it.gameId }.toSet()

    val todaySchedules = schedules.filter { it.dayOfWeek == todayDayOfWeek && activeGameIds.contains(it.gameId) }
    val tomorrowSchedules = schedules.filter { it.dayOfWeek == tomorrowDayOfWeek && activeGameIds.contains(it.gameId) }

    val todayGameNames = todaySchedules.mapNotNull { sched -> games.find { it.id == sched.gameId }?.name }.distinct()
    val tomorrowGameNames = tomorrowSchedules.mapNotNull { sched -> games.find { it.id == sched.gameId }?.name }.distinct()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("تفاصيل العضو") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف")
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
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("المعلومات الشخصية")
            AppCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemberAvatar(name = member.name)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(member.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(member.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("الجنس: ${member.gender}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                    Text("تاريخ الانضمام: ${formatEpochDay(member.joinDate)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            SectionHeader("الاشتراكات الحالية")
            if (subscriptions.isEmpty()) {
                EmptyState(icon = Icons.Default.Search, title = "لا يوجد اشتراكات", subtitle = "هذا العضو غير مسجل في أي لعبة حالياً.")
            } else {
                subscriptions.forEach { sub ->
                    val game = games.find { it.id == sub.gameId }
                    val gameName = game?.name ?: "لعبة #${sub.gameId}"
                    AppCard(modifier = Modifier.padding(bottom = 8.dp)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(gameName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                StatusChip(text = sub.planType.name, status = "نشط")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("السعر: $${sub.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${formatEpochDay(sub.startDate)} إلى ${formatEpochDay(sub.endDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader("جدول التدريب")
            AppCard {
                Column {
                    val today = getArabicDayOfWeek(0)
                    val tomorrow = getArabicDayOfWeek(1)

                    val todayGamesStr = if (todayGameNames.isNotEmpty()) todayGameNames.joinToString(", ") else "لا يوجد تدريب مجدول"
                    val tomorrowGamesStr = if (tomorrowGameNames.isNotEmpty()) tomorrowGameNames.joinToString(", ") else "لا يوجد تدريب مجدول"

                    Text("اليوم ($today): $todayGamesStr", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("غداً ($tomorrow): $tomorrowGamesStr", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onShowSchedule, modifier = Modifier.fillMaxWidth()) {
                        Text("إدارة الجدول وعرض التفاصيل")
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditMemberDialog(
            member = member,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, gender, dob, joinDate ->
                val birthEpoch = if (dob.isBlank()) null else parseEpochDay(dob)
                val joinEpoch = if (joinDate.isBlank()) member.joinDate else parseEpochDay(joinDate)
                onEdit(member.copy(name = name, phone = phone, gender = gender, birthDate = birthEpoch, joinDate = joinEpoch))
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف العضو") },
            text = { Text("هل أنت متأكد أنك تريد حذف ${member.name}؟") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, gender: String, dob: String, joinDate: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("ذكر") }
    var dob by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf(formatEpochDay(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()))) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة عضو جديد") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (showError) {
                    Text(
                        text = "الاسم ورقم الهاتف مطلوبان.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("الاسم الكامل *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = com.example.simplecalc.ui.components.appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; showError = false },
                    label = { Text("رقم الهاتف *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = com.example.simplecalc.ui.components.appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Text("الجنس", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val genders = listOf("ذكر", "أنثى")
                    genders.forEach { g ->
                        val isSelected = gender == g
                        OutlinedButton(
                            onClick = { gender = g },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) {
                            Text(g)
                        }
                    }
                }
                DatePickerField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = "تاريخ الميلاد",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                DatePickerField(
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = "تاريخ الانضمام",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        showError = true
                    } else {
                        onSave(name, phone, gender, dob, joinDate)
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

@Composable
fun EditMemberDialog(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, gender: String, dob: String, joinDate: String) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var phone by remember { mutableStateOf(member.phone) }
    var gender by remember { mutableStateOf(member.gender.ifEmpty { "ذكر" }) }
    var dob by remember { mutableStateOf(formatEpochDay(member.birthDate)) }
    var joinDate by remember { mutableStateOf(formatEpochDay(member.joinDate)) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل العضو") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (showError) {
                    Text(
                        text = "الاسم ورقم الهاتف مطلوبان.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("الاسم الكامل *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = com.example.simplecalc.ui.components.appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; showError = false },
                    label = { Text("رقم الهاتف *") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = com.example.simplecalc.ui.components.appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Text("الجنس", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val genders = listOf("ذكر", "أنثى")
                    genders.forEach { g ->
                        val isSelected = gender == g
                        OutlinedButton(
                            onClick = { gender = g },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) {
                            Text(g)
                        }
                    }
                }
                DatePickerField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = "تاريخ الميلاد",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                DatePickerField(
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = "تاريخ الانضمام",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        showError = true
                    } else {
                        onSave(name, phone, gender, dob, joinDate)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScheduleScreen(
    member: MemberEntity,
    viewModel: MembersViewModel,
    onBack: () -> Unit
) {
    val schedules by viewModel.getMemberSchedulesFlow(member.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val games by viewModel.getAllActiveGamesFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("جدول تدريب: ${member.name}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(daysOfWeekArabic.size) { index ->
                val dayName = daysOfWeekArabic[index]
                val dayOfWeekVal = index + 1 // 1=Sunday...
                val daySchedules = schedules.filter { it.dayOfWeek == dayOfWeekVal }
                val dayGameNames = daySchedules.mapNotNull { sched -> games.find { it.id == sched.gameId }?.name }.distinct()

                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    onClick = { selectedDayIndex = index }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(dayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            val summary = if (dayGameNames.isNotEmpty()) dayGameNames.joinToString(", ") else "لا يوجد تدريب"
                            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { selectedDayIndex = index }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل الجدول")
                        }
                    }
                }
            }
        }
    }

    if (selectedDayIndex != null) {
        val index = selectedDayIndex!!
        val dayName = daysOfWeekArabic[index]
        val dayOfWeekVal = index + 1

        AddEditScheduleDialog(
            dayName = dayName,
            games = games,
            currentSchedules = schedules.filter { it.dayOfWeek == dayOfWeekVal },
            onDismiss = { selectedDayIndex = null },
            onSave = { gameId, trainingTypeIds ->
                viewModel.saveScheduleForMemberAndGame(member.id, gameId, trainingTypeIds, dayOfWeekVal)
                selectedDayIndex = null
            }
        )
    }
}

@Composable
fun AddEditScheduleDialog(
    dayName: String,
    games: List<GameEntity>,
    currentSchedules: List<TrainingScheduleEntity>,
    onDismiss: () -> Unit,
    onSave: (gameId: Long, trainingTypeIds: List<Long?>) -> Unit
) {
    var selectedGame by remember { mutableStateOf<GameEntity?>(games.firstOrNull()) }
    var gameMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("جدول يوم $dayName") },
        text = {
            Column {
                Text("اختر اللعبة للتدريب في هذا اليوم:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { gameMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedGame?.name ?: "اختر اللعبة")
                    }
                    DropdownMenu(
                        expanded = gameMenuExpanded,
                        onDismissRequest = { gameMenuExpanded = false }
                    ) {
                        games.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                onClick = {
                                    selectedGame = g
                                    gameMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val g = selectedGame
                    if (g != null) {
                        onSave(g.id, listOf(null))
                    }
                },
                enabled = selectedGame != null
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MembersScreenPreview() {
    SimpleCalcTheme {
        MembersScreen()
    }
}