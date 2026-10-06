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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

fun getCurrentTime(): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
}

data class AttendanceGameRef(
    val subscriptionId: Int,
    val gameName: String
)

data class Attendance(
    val id: Int,
    val memberId: Int,
    val memberName: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val status: String,
    val date: String,
    val games: List<AttendanceGameRef> = emptyList(),
    val weight: Double? = null,
    val notes: String = ""
)

val initialMockAttendance = listOf(
    Attendance(1, 1, "أحمد محمد", "08:00 ص", null, "حاضر", getCurrentDate(), listOf(AttendanceGameRef(1, "رفع الأثقال")), 75.5, "تمرين صباحي"),
    Attendance(2, 2, "سارة أحمد", "07:30 ص", "09:30 ص", "منصرف", getCurrentDate(), listOf(AttendanceGameRef(2, "سباحة")), null, "")
)

val globalAttendance = mutableStateListOf(*initialMockAttendance.toTypedArray())

@Composable
fun AttendanceScreen() {
    var selectedFilter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "حاضر", "منصرف")
    var showCheckInDialog by remember { mutableStateOf(false) }
    var confirmCheckOutRecord by remember { mutableStateOf<Attendance?>(null) }

    val todayRecords = globalAttendance.filter { it.date == getCurrentDate() }
    
    val filteredRecords = todayRecords.filter {
        when (selectedFilter) {
            "حاضر" -> it.status == "حاضر"
            "منصرف" -> it.status == "منصرف"
            else -> true
        }
    }

    val totalAttendance = todayRecords.size
    val currentlyInside = todayRecords.count { it.status == "حاضر" }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCheckInDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = "تسجيل حضور") },
                text = { Text("تسجيل حضور") }
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
                        Text("حضور اليوم", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalAttendance", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("بالداخل حالياً", style = MaterialTheme.typography.bodyMedium)
                        Text("$currentlyInside", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
            if (filteredRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Text("لا يوجد سجلات حضور اليوم.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        AttendanceItem(
                            record = record,
                            onCheckOut = { confirmCheckOutRecord = record }
                        )
                    }
                }
            }
        }
    }

    if (showCheckInDialog) {
        CheckInDialog(
            onDismiss = { showCheckInDialog = false },
            onSaveNew = { member, weight, notes, games ->
                val newId = (globalAttendance.maxOfOrNull { it.id } ?: 0) + 1
                globalAttendance.add(
                    Attendance(
                        id = newId,
                        memberId = member.id,
                        memberName = member.name,
                        checkInTime = getCurrentTime(),
                        checkOutTime = null,
                        status = "حاضر",
                        date = getCurrentDate(),
                        games = games,
                        weight = weight,
                        notes = notes
                    )
                )
                if (weight != null) {
                    val newMeasId = (globalMeasurements.maxOfOrNull { it.id } ?: 0) + 1
                    globalMeasurements.add(MemberMeasurement(newMeasId, member.id, getCurrentDate(), weight, notes))
                }
                showCheckInDialog = false
            },
            onCheckOutExisting = { existingRecord ->
                val index = globalAttendance.indexOfFirst { it.id == existingRecord.id }
                if (index != -1) {
                    globalAttendance[index] = existingRecord.copy(
                        checkOutTime = getCurrentTime(),
                        status = "منصرف"
                    )
                }
                showCheckInDialog = false
            }
        )
    }

    if (confirmCheckOutRecord != null) {
        AlertDialog(
            onDismissRequest = { confirmCheckOutRecord = null },
            title = { Text("تأكيد تسجيل الانصراف") },
            text = { Text("هل أنت متأكد أنك تريد تسجيل انصراف ${confirmCheckOutRecord?.memberName}؟") },
            confirmButton = {
                TextButton(onClick = {
                    val index = globalAttendance.indexOfFirst { it.id == confirmCheckOutRecord?.id }
                    if (index != -1) {
                        globalAttendance[index] = globalAttendance[index].copy(
                            checkOutTime = getCurrentTime(),
                            status = "منصرف"
                        )
                    }
                    confirmCheckOutRecord = null
                }) {
                    Text("تسجيل انصراف")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCheckOutRecord = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun AttendanceItem(record: Attendance, onCheckOut: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.memberName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val statusColor = if (record.status == "حاضر") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Text(
                    text = record.status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            if (record.games.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الألعاب: ${record.games.joinToString("، ") { it.gameName }}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "وقت الحضور: ${record.checkInTime}", style = MaterialTheme.typography.bodyMedium)
                    if (record.checkOutTime != null) {
                        Text(text = "وقت الانصراف: ${record.checkOutTime}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (record.weight != null) {
                        Text(text = "الوزن: ${record.weight} كجم", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (record.notes.isNotEmpty()) {
                        Text(text = "ملاحظات: ${record.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (record.status == "حاضر") {
                    OutlinedButton(onClick = onCheckOut) {
                        Text("تسجيل انصراف")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckInDialog(
    onDismiss: () -> Unit,
    onSaveNew: (member: Member, weight: Double?, notes: String, selectedGames: List<AttendanceGameRef>) -> Unit,
    onCheckOutExisting: (Attendance) -> Unit
) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    var weightStr by remember { mutableStateOf("") }
    var notesStr by remember { mutableStateOf("") }

    val todayDate = getCurrentDate()
    val todayDay = getArabicDayOfWeek(0)

    // Check if selected member is currently checked in today
    val activeRecord = remember(selectedMember) {
        if (selectedMember == null) null
        else globalAttendance.find { 
            it.memberId == selectedMember!!.id && 
            it.date == todayDate && 
            it.status == "حاضر" 
        }
    }
    val isCheckedIn = activeRecord != null

    // Active subscriptions for selected member
    val memberActiveSubs = remember(selectedMember) {
        if (selectedMember == null) emptyList()
        else globalSubscriptions.filter { 
            it.memberId == selectedMember!!.id && getSubscriptionStatus(it) == "نشط" 
        }
    }

    // Today's scheduled game IDs for selected member
    val todayScheduledGameIds = remember(selectedMember) {
        if (selectedMember == null) emptySet()
        else globalSchedules
            .filter { it.memberId == selectedMember!!.id && it.dayOfWeek == todayDay }
            .map { it.gameId }
            .toSet()
    }

    // Track selected subscription IDs for check-in
    val selectedSubIds = remember(selectedMember) {
        mutableStateListOf<Int>().apply {
            if (selectedMember != null) {
                val activeForMember = globalSubscriptions.filter { 
                    it.memberId == selectedMember!!.id && getSubscriptionStatus(it) == "نشط" 
                }
                val todayDayStr = getArabicDayOfWeek(0)
                val schedGameIds = globalSchedules
                    .filter { it.memberId == selectedMember!!.id && it.dayOfWeek == todayDayStr }
                    .map { it.gameId }
                    .toSet()

                val suggested = activeForMember.filter { it.gameId in schedGameIds }
                if (suggested.isNotEmpty()) {
                    addAll(suggested.map { it.id })
                } else {
                    addAll(activeForMember.map { it.id })
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCheckedIn) "تسجيل انصراف عضو" else "تسجيل حضور جديد") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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

                if (selectedMember != null) {
                    if (isCheckedIn) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("العضو متواجد بالداخل حالياً!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("وقت الحضور: ${activeRecord.checkInTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    } else {
                        // Games Selection Section
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("الألعاب المتاحة والاشتراكات:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        if (memberActiveSubs.isEmpty()) {
                            Text("لا توجد اشتراكات نشطة لهذا العضو.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
                        } else {
                            memberActiveSubs.forEach { sub ->
                                val used = getUsedSessions(sub.id)
                                val remaining = (sub.totalSessions - used).coerceAtLeast(0)
                                val isScheduledToday = sub.gameId in todayScheduledGameIds
                                val isChecked = selectedSubIds.contains(sub.id)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedSubIds.remove(sub.id)
                                            else selectedSubIds.add(sub.id)
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedSubIds.add(sub.id)
                                            else selectedSubIds.remove(sub.id)
                                        }
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(sub.gameName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            if (isScheduledToday) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.extraSmall
                                                ) {
                                                    Text("جدول اليوم", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                }
                                            }
                                        }
                                        Text("الجلسات: $used مستخدمة / $remaining متبقية من ${sub.totalSessions}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Optional Weight & Notes
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("الوزن - كجم (اختياري)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = notesStr,
                            onValueChange = { notesStr = it },
                            label = { Text("ملاحظات الزيارة (اختياري)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedMember != null) {
                        if (isCheckedIn) {
                            onCheckOutExisting(activeRecord)
                        } else {
                            val chosenSubs = memberActiveSubs.filter { selectedSubIds.contains(it.id) }
                            val attendanceGames = chosenSubs.map { AttendanceGameRef(it.id, it.gameName) }
                            val parsedWeight = weightStr.toDoubleOrNull()
                            onSaveNew(selectedMember!!, parsedWeight, notesStr, attendanceGames)
                        }
                    }
                },
                enabled = selectedMember != null && (isCheckedIn || (memberActiveSubs.isNotEmpty() && selectedSubIds.isNotEmpty()))
            ) {
                Text(if (isCheckedIn) "تسجيل انصراف" else "تسجيل حضور")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
