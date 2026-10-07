package com.example.simplecalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplecalc.data.repository.AttendanceRepository
import com.example.simplecalc.data.repository.MemberRepository
import com.example.simplecalc.data.repository.PaymentRepository
import com.example.simplecalc.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.util.concurrent.TimeUnit

data class DashboardUiState(
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val todayCheckIns: Int = 0,
    val currentlyInside: Int = 0,
    val sessionsUsedToday: Int = 0,
    val todayPay: Double = 0.0,
    val expSoonCount: Int = 0,
    val activeSubsCount: Int = 0,
    val upcomingSubsCount: Int = 0,
    val expiredSubsCount: Int = 0,
    val recentActivities: List<String> = emptyList()
)

class DashboardViewModel(
    memberRepository: MemberRepository,
    subscriptionRepository: SubscriptionRepository,
    attendanceRepository: AttendanceRepository,
    paymentRepository: PaymentRepository
) : ViewModel() {

    private val todayStartMillis = run {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        (now / dayMillis) * dayMillis
    }

    private val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())

    val uiState: StateFlow<DashboardUiState> = combine(
        memberRepository.getAllActiveFlow(),
        subscriptionRepository.getAllSubscriptionsFlow(),
        attendanceRepository.getTodayAttendanceWithGamesFlow(todayStartMillis),
        paymentRepository.getAllPaymentsFlow()
    ) { members, subscriptions, todayAttendance, payments ->

        val activeMembersCount = members.size

        val todayCheckInsCount = todayAttendance.size
        val currentlyInsideCount = todayAttendance.count { it.attendance.checkOut == null }
        val sessionsUsedCount = todayAttendance.sumOf { it.games.size }

        val todayPaymentsSum = payments.filter { it.date == todayEpochDay }.sumOf { it.amount }

        val activeSubs = subscriptions.count { it.startDate <= todayEpochDay && it.endDate >= todayEpochDay }
        val upcomingSubs = subscriptions.count { it.startDate > todayEpochDay }
        val expiredSubs = subscriptions.count { it.endDate < todayEpochDay }
        val expSoon = subscriptions.count {
            it.startDate <= todayEpochDay && it.endDate >= todayEpochDay && (it.endDate - todayEpochDay) in 0..7
        }

        val activities = mutableListOf<String>()
        members.firstOrNull()?.let { activities.add("عضو جديد: ${it.name}") }
        subscriptions.firstOrNull()?.let { activities.add("اشتراك جديد: $it") }
        payments.firstOrNull()?.let { activities.add("دفعة: $${it.amount}") }

        DashboardUiState(
            totalMembers = members.size,
            activeMembers = activeMembersCount,
            todayCheckIns = todayCheckInsCount,
            currentlyInside = currentlyInsideCount,
            sessionsUsedToday = sessionsUsedCount,
            todayPay = todayPaymentsSum,
            expSoonCount = expSoon,
            activeSubsCount = activeSubs,
            upcomingSubsCount = upcomingSubs,
            expiredSubsCount = expiredSubs,
            recentActivities = activities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubsubscribed(5000),
        initialValue = DashboardUiState()
    )
}

private fun SharingStarted.Companion.WhileSubsubscribed(i: Int): SharingStarted = WhileSubscribed(i.toLong())