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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import java.util.Calendar

data class Member(
    val id: Int,
    val name: String,
    val phone: String,
    val gender: String,
    val dob: String,
    val joinDate: String,
    val status: String,
    val expiryDate: String
)

val initialMockMembers = listOf(
    Member(1, "أحمد محمد", "123-456-7890", "ذكر", "1990-01-01", "2024-01-01", "نشط", "2024-12-31"),
    Member(2, "سارة أحمد", "098-765-4321", "أنثى", "1995-05-05", "2023-10-15", "غير نشط", "2023-10-15"),
    Member(3, "خالد محمود", "555-123-4567", "ذكر", "1985-12-08", "2024-06-30", "نشط", "2025-06-30")
)

data class TrainingSchedule(
    val id: Int,
    val memberId: Int,
    val gameId: Int,
    val dayOfWeek: String,
    val trainingNames: List<String>
)

val globalSchedules = mutableStateListOf(
    TrainingSchedule(1, 1, 1, "الأحد", listOf("أكتاف")),
    TrainingSchedule(2, 1, 1, "الإثنين", listOf("بطن")),
    TrainingSchedule(3, 1, 1, "الثلاثاء", listOf("صدر", "ذراعين")),
    TrainingSchedule(4, 1, 1, "الأربعاء", listOf("أرجل")),
    TrainingSchedule(5, 1, 1, "الخميس", listOf("ظهر"))
)

data class MemberMeasurement(
    val id: Int,
    val memberId: Int,
    val date: String,
    val weight: Double?,
    val notes: String
)

val globalMeasurements = mutableStateListOf<MemberMeasurement>()

val globalMembers = mutableStateListOf(*initialMockMembers.toTypedArray())

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
fun MembersScreen() {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var showScheduleScreen by remember { mutableStateOf(false) }

    if (showScheduleScreen && selectedMember != null) {
        TrainingScheduleScreen(
            member = selectedMember!!,
            onBack = { showScheduleScreen = false }
        )
    } else if (selectedMember != null) {
        MemberDetailsScreen(
            member = selectedMember!!,
            onBack = { selectedMember = null },
            onEdit = { updatedMember ->
                val index = globalMembers.indexOfFirst { it.id == updatedMember.id }
                if (index != -1) {
                    globalMembers[index] = updatedMember
                }
                selectedMember = updatedMember
            },
            onDelete = {
                globalMembers.removeAll { it.id == selectedMember!!.id }
                selectedMember = null
            },
            onShowSchedule = { showScheduleScreen = true }
        )
    } else {
        MembersListScreen(
            onMemberClick = { selectedMember = it }
        )
    }
}

@Composable
fun MembersListScreen(onMemberClick: (Member) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredMembers = globalMembers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery, ignoreCase = true)
    }

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
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("البحث بالاسم أو رقم الهاتف") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                singleLine = true
            )

            if (filteredMembers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لم يتم العثور على أعضاء.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredMembers, key = { it.id }) { member ->
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
                val newId = (globalMembers.maxOfOrNull { it.id } ?: 0) + 1
                globalMembers.add(
                    Member(
                        id = newId,
                        name = name,
                        phone = phone,
                        gender = gender,
                        dob = dob,
                        joinDate = joinDate, // Mock default join date
                        status = "نشط",
                        expiryDate = "2025-12-31" // Mock default expiry date
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MemberItem(member: Member, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "الهاتف: ${member.phone}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الحالة: ${member.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (member.status == "نشط") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "ينتهي: ${member.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailsScreen(
    member: Member,
    onBack: () -> Unit,
    onEdit: (Member) -> Unit,
    onDelete: () -> Unit,
    onShowSchedule: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("المعلومات الشخصية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("الاسم: ${member.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("الهاتف: ${member.phone}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("الجنس: ${member.gender}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                    Text("تاريخ الميلاد: ${member.dob}", style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("الاشتراكات الحالية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val memberSubs = globalSubscriptions.filter { it.memberId == member.id }
            if (memberSubs.isEmpty()) {
                Text("لا توجد اشتراكات", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                memberSubs.forEach { sub ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(sub.gameName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("خطة الاشتراك: ${sub.planType} | السعر: $${sub.price}", style = MaterialTheme.typography.bodyMedium)
                            val statusColor = if (sub.status == "نشط") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            Text("الحالة: ${sub.status}", style = MaterialTheme.typography.bodyMedium, color = statusColor)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("جدول التدريب", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val today = getArabicDayOfWeek(0)
                    val tomorrow = getArabicDayOfWeek(1)
                    val todaySchedule = globalSchedules.filter { it.memberId == member.id && it.dayOfWeek == today }
                    val tomorrowSchedule = globalSchedules.filter { it.memberId == member.id && it.dayOfWeek == tomorrow }
                    
                    val todayText = if (todaySchedule.isEmpty()) "راحة" else todaySchedule.flatMap { it.trainingNames }.joinToString("، ")
                    val tomorrowText = if (tomorrowSchedule.isEmpty()) "راحة" else tomorrowSchedule.flatMap { it.trainingNames }.joinToString("، ")

                    Text("اليوم ($today): $todayText", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("غداً ($tomorrow): $tomorrowText", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(12.dp))
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
                onEdit(member.copy(name = name, phone = phone, gender = gender, dob = dob, joinDate = joinDate))
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
    var joinDate by remember { mutableStateOf(getCurrentDate()) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة عضو جديد") },
        text = {
            Column {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; showError = false },
                    label = { Text("رقم الهاتف *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Text("الجنس", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = gender == "ذكر",
                        onClick = { gender = "ذكر" }
                    )
                    Text("ذكر", modifier = Modifier.clickable { gender = "ذكر" }.padding(end = 16.dp))
                    
                    RadioButton(
                        selected = gender == "أنثى",
                        onClick = { gender = "أنثى" }
                    )
                    Text("أنثى", modifier = Modifier.clickable { gender = "أنثى" })
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
            TextButton(
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
    member: Member,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, gender: String, dob: String, joinDate: String) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var phone by remember { mutableStateOf(member.phone) }
    var gender by remember { mutableStateOf(member.gender.ifEmpty { "ذكر" }) }
    var dob by remember { mutableStateOf(member.dob) }
    var joinDate by remember { mutableStateOf(member.joinDate) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل العضو") },
        text = {
            Column {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; showError = false },
                    label = { Text("رقم الهاتف *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("الجنس") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
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
            TextButton(
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
    member: Member,
    onBack: () -> Unit
) {
    val memberSubs = globalSubscriptions.filter { it.memberId == member.id }
    val availableTypes = memberSubs.mapNotNull { sub -> globalGames.find { it.name == sub.gameName } }
        .flatMap { game -> globalTrainings.filter { it.gameId == game.id }.map { it.name } }
        .distinct()
        .ifEmpty { listOf("تدريب عام") }
    val options = availableTypes + listOf("راحة")
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("جدول تدريب: ${member.name}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(daysOfWeekArabic) { day ->
                var expanded by remember { mutableStateOf(false) }
                
                // Get existing schedule for this day
                val existingSchedule = globalSchedules.find { it.memberId == member.id && it.dayOfWeek == day }
                val currentType = existingSchedule?.trainingNames?.firstOrNull() ?: "راحة"
                
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(day, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(currentType) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) }, 
                                    onClick = { 
                                        if (existingSchedule != null) {
                                            val index = globalSchedules.indexOf(existingSchedule)
                                            if (opt == "راحة") {
                                                globalSchedules.removeAt(index)
                                            } else {
                                                globalSchedules[index] = existingSchedule.copy(trainingNames = listOf(opt))
                                            }
                                        } else if (opt != "راحة") {
                                            val newId = (globalSchedules.maxOfOrNull { it.id } ?: 0) + 1
                                            // Assigning default gameId = 1 for simplicity since this is a quick mock integration
                                            globalSchedules.add(TrainingSchedule(newId, member.id, 1, day, listOf(opt)))
                                        }
                                        expanded = false 
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MembersScreenPreview() {
    SimpleCalcTheme {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            MembersScreen()
        }
    }
}
