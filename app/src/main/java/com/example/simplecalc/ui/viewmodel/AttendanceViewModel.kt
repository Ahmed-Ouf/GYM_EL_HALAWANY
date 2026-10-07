package com.example.simplecalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplecalc.data.local.dao.AttendanceWithGames
import com.example.simplecalc.data.local.entity.AttendanceEntity
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.MemberMeasurementEntity
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.repository.AttendanceRepository
import com.example.simplecalc.data.repository.GameRepository
import com.example.simplecalc.data.repository.MemberMeasurementRepository
import com.example.simplecalc.data.repository.MemberRepository
import com.example.simplecalc.data.repository.OperationResult
import com.example.simplecalc.data.repository.SubscriptionRepository
import com.example.simplecalc.data.repository.TrainingScheduleRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class AttendanceUiState(
    val todayAttendance: List<AttendanceWithGames> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val selectedFilter: String = "الكل",
    val totalToday: Int = 0,
    val currentlyInside: Int = 0
)

data class SuggestedGamesInfo(
    val suggestedGameIds: List<Long>,
    val activeSubscriptions: List<SubscriptionEntity>,
    val hasWarningForGame: Map<Long, Boolean> // true if game does NOT have active sub
)

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val scheduleRepository: TrainingScheduleRepository,
    private val gameRepository: GameRepository,
    private val measurementRepository: MemberMeasurementRepository
) : ViewModel() {

    val selectedFilter = MutableStateFlow("الكل")

    private val _checkInError = MutableSharedFlow<String>()
    val checkInError: SharedFlow<String> = _checkInError.asSharedFlow()

    private val todayStartMillis = run {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        (now / dayMillis) * dayMillis
    }

    val uiState: StateFlow<AttendanceUiState> = combine(
        attendanceRepository.getTodayAttendanceWithGamesFlow(todayStartMillis),
        memberRepository.getAllActiveFlow(),
        selectedFilter
    ) { attendanceList, members, filter ->
        val filtered = when (filter) {
            "حاضر" -> attendanceList.filter { it.attendance.checkOut == null }
            "منصرف" -> attendanceList.filter { it.attendance.checkOut != null }
            else -> attendanceList
        }
        AttendanceUiState(
            todayAttendance = filtered,
            members = members,
            selectedFilter = filter,
            totalToday = attendanceList.size,
            currentlyInside = attendanceList.count { it.attendance.checkOut == null }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AttendanceUiState()
    )

    fun onFilterSelected(filter: String) {
        selectedFilter.value = filter
    }

    suspend fun getSuggestedGamesForMember(memberId: Long): SuggestedGamesInfo {
        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val activeSubs = subscriptionRepository.getActiveForMemberFlow(memberId, todayEpochDay).first()
        val activeGameIds = activeSubs.map { it.gameId }.toSet()

        val dayOfWeekVal = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todaySchedule = scheduleRepository.getForMemberAndDayFlow(memberId, dayOfWeekVal).first()
        val scheduledGameIds = todaySchedule.map { it.gameId }.distinct()

        // Auto-select scheduled games that have an active subscription
        val autoSelected = scheduledGameIds.filter { it in activeGameIds }

        // If no schedule today, auto-select all active subscriptions
        val finalSelected = if (autoSelected.isNotEmpty()) {
            autoSelected
        } else {
            activeGameIds.toList()
        }

        val warnings = mutableMapOf<Long, Boolean>()
        scheduledGameIds.forEach { gameId ->
            warnings[gameId] = (gameId !in activeGameIds)
        }

        return SuggestedGamesInfo(
            suggestedGameIds = finalSelected,
            activeSubscriptions = activeSubs,
            hasWarningForGame = warnings
        )
    }

    fun checkIn(memberId: Long, gameIds: List<Long>, weight: Double? = null, notes: String? = null) {
        viewModelScope.launch {
            val attendance = AttendanceEntity(
                memberId = memberId,
                checkIn = System.currentTimeMillis()
            )
            val result = attendanceRepository.checkIn(attendance, gameIds)
            when (result) {
                is OperationResult.Success -> {
                    if (weight != null) {
                        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
                        measurementRepository.addMeasurement(
                            MemberMeasurementEntity(
                                memberId = memberId,
                                date = todayEpochDay,
                                weight = weight,
                                notes = notes
                            )
                        )
                    }
                }
                is OperationResult.OverlapError -> {
                    _checkInError.emit(result.message)
                }
                is OperationResult.ArchivedMemberError -> {
                    _checkInError.emit(result.message)
                }
                is OperationResult.Error -> {
                    _checkInError.emit(result.message)
                }
            }
        }
    }

    fun checkOut(attendanceId: Long) {
        viewModelScope.launch {
            attendanceRepository.checkOut(attendanceId)
        }
    }
}