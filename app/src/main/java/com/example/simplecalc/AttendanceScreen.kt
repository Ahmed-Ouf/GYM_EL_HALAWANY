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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplecalc.data.local.dao.AttendanceWithGames
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.ui.AppViewModelProvider
import com.example.simplecalc.ui.components.AppCard
import com.example.simplecalc.ui.components.EmptyState
import com.example.simplecalc.ui.components.GameChip
import com.example.simplecalc.ui.components.MemberAvatar
import com.example.simplecalc.ui.components.SearchableMemberDropdown
import com.example.simplecalc.ui.components.StatCard
import com.example.simplecalc.ui.components.StatusChip
import com.example.simplecalc.ui.components.appTextFieldColors
import com.example.simplecalc.ui.theme.SimpleCalcTheme
import com.example.simplecalc.ui.theme.StatusColorsLight
import com.example.simplecalc.ui.viewmodel.AttendanceUiState
import com.example.simplecalc.ui.viewmodel.AttendanceViewModel
import com.example.simplecalc.ui.viewmodel.SuggestedGamesInfo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentTime(): String {
    return SimpleDateFormat("hh:mm a", Locale.US).format(Date()).replace("AM", "ص").replace("PM", "م")
}

fun formatTime(millis: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.US).format(Date(millis)).replace("AM", "ص").replace("PM", "م")
}

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCheckInDialog by remember { mutableStateOf(false) }
    var confirmCheckOutRecord by remember { mutableStateOf<AttendanceWithGames?>(null) }

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
            // Stat Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Default.CheckCircle,
                    value = "${uiState.totalToday}",
                    label = "حضور اليوم",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.Home,
                    value = "${uiState.currentlyInside}",
                    label = "بالداخل حالياً",
                    accentColor = StatusColorsLight.Active,
                    modifier = Modifier.weight(1f)
                )
            }

            // Filters
            val filters = listOf("الكل", "حاضر", "منصرف")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
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
            if (uiState.todayAttendance.isEmpty()) {
                EmptyState(icon = Icons.Default.CheckCircle, title = "لا يوجد حضور", subtitle = "لم يتم تسجيل أي حضور اليوم في هذا القسم.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    items(uiState.todayAttendance, key = { it.attendance.id }) { item ->
                        AttendanceItem(
                            record = item,
                            members = uiState.members,
                            onCheckOut = { confirmCheckOutRecord = item }
                        )
                    }
                }
            }
        }
    }

    if (showCheckInDialog) {
        CheckInDialog(
            members = uiState.members,
            onDismiss = { showCheckInDialog = false },
            viewModel = viewModel,
            onSuccess = { showCheckInDialog = false }
        )
    }

    if (confirmCheckOutRecord != null) {
        val member = uiState.members.find { it.id == confirmCheckOutRecord!!.attendance.memberId }
        val memberName = member?.name ?: "العضو"
        AlertDialog(
            onDismissRequest = { confirmCheckOutRecord = null },
            title = { Text("تأكيد تسجيل الانصراف") },
            text = { Text("هل أنت متأكد أنك تريد تسجيل انصراف $memberName؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.checkOut(confirmCheckOutRecord!!.attendance.id)
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
fun AttendanceItem(
    record: AttendanceWithGames,
    members: List<MemberEntity>,
    onCheckOut: () -> Unit
) {
    val member = members.find { it.id == record.attendance.memberId }
    val memberName = member?.name ?: "عضو #${record.attendance.memberId}"
    val isInside = record.attendance.checkOut == null
    val statusText = if (isInside) "حاضر" else "منصرف"

    AppCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(name = memberName)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = memberName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusChip(text = statusText, status = statusText)
            }

            if (record.games.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    record.games.forEach { game ->
                        GameChip(name = game.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "وقت الحضور: ${formatTime(record.attendance.checkIn)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (record.attendance.checkOut != null) {
                        Text(text = "وقت الانصراف: ${formatTime(record.attendance.checkOut)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (isInside) {
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
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    viewModel: AttendanceViewModel,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedMember by remember { mutableStateOf<MemberEntity?>(null) }

    var suggestedInfo by remember { mutableStateOf<SuggestedGamesInfo?>(null) }
    val selectedGameIds = remember { mutableStateListOf<Long>() }

    var weightStr by remember { mutableStateOf("") }
    var notesStr by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedMember) {
        val m = selectedMember
        if (m != null) {
            val info = viewModel.getSuggestedGamesForMember(m.id)
            suggestedInfo = info
            selectedGameIds.clear()
            selectedGameIds.addAll(info.suggestedGameIds)
        } else {
            suggestedInfo = null
            selectedGameIds.clear()
        }
    }

    LaunchedEffect(viewModel.checkInError) {
        viewModel.checkInError.collect { err ->
            errorMessage = err
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل حضور جديد") },
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
                SearchableMemberDropdown(
                    members = members,
                    selectedMember = selectedMember,
                    onMemberSelected = { 
                        selectedMember = it
                        suggestedInfo = null
                        selectedGameIds.clear()
                        errorMessage = null
                    },
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                )

                if (selectedMember != null && suggestedInfo != null) {
                    val info = suggestedInfo!!
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الألعاب المتاحة والاشتراكات:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    if (info.activeSubscriptions.isEmpty()) {
                        Text("تحذير: لا توجد اشتراكات نشطة لهذا العضو.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        info.activeSubscriptions.forEach { sub ->
                            val isChecked = selectedGameIds.contains(sub.gameId)
                            val hasWarn = info.hasWarningForGame[sub.gameId] == true

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedGameIds.remove(sub.gameId)
                                        else selectedGameIds.add(sub.gameId)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedGameIds.add(sub.gameId)
                                        else selectedGameIds.remove(sub.gameId)
                                    }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text("لعبة #${sub.gameId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    if (hasWarn) {
                                        Text("تنبيه: غير مجدولة اليوم، ولكن يوجد اشتراك نشط", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("الوزن - كجم (اختياري)") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("ملاحظات الزيارة (اختياري)") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = com.example.simplecalc.ui.components.appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = selectedMember
                    if (m != null && selectedGameIds.isNotEmpty()) {
                        val parsedWeight = weightStr.toDoubleOrNull()
                        viewModel.checkIn(
                            memberId = m.id,
                            gameIds = selectedGameIds,
                            weight = parsedWeight,
                            notes = notesStr.ifBlank { null }
                        )
                        onSuccess()
                    } else if (selectedGameIds.isEmpty()) {
                        errorMessage = "يرجى اختيار لعبة واحدة على الأقل."
                    }
                },
                enabled = selectedMember != null && selectedGameIds.isNotEmpty()
            ) {
                Text("تسجيل حضور")
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
fun AttendanceScreenPreview() {
    SimpleCalcTheme {
        AttendanceScreen()
    }
}