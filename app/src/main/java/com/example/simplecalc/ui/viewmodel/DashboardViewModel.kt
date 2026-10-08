package com.example.simplecalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplecalc.data.local.entity.PlanType
import com.example.simplecalc.data.repository.AttendanceRepository
import com.example.simplecalc.data.repository.DashboardRepository
import com.example.simplecalc.data.repository.MemberRepository
import com.example.simplecalc.data.repository.PaymentRepository
import com.example.simplecalc.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    dashboardRepository: DashboardRepository,
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

    init {
        viewModelScope.launch {
            attendanceRepository.closeStaleSessions(todayStartMillis)
        }
    }

    private val todayEpochDay = LocalDate.now().toEpochDay()

    val uiState: StateFlow<DashboardUiState> = combine(
        dashboardRepository.getDashboardStatsFlow(todayEpochDay, todayStartMillis),
        memberRepository.getAllActiveFlow(),
        subscriptionRepository.getAllSubscriptionsFlow(),
        attendanceRepository.getTodayAttendanceWithGamesFlow(todayStartMillis),
        paymentRepository.getAllPaymentsFlow()
    ) { stats, members, subscriptions, todayAttendance, payments ->

        val sessionsUsedCount = todayAttendance.sumOf { it.games.size }

        val activities = mutableListOf<String>()
        members.firstOrNull()?.let { activities.add("عضو جديد: ${it.name}") }
        subscriptions.firstOrNull()?.let { sub ->
            val subMember = members.find { m -> m.id == sub.memberId }
            val planTypeStr = when (sub.planType) {
                PlanType.MONTHLY -> "شهري"
                PlanType.QUARTERLY -> "3 أشهر"
                PlanType.YEARLY -> "سنوي"
            }
            if (subMember != null) {
                activities.add("اشتراك جديد: ${subMember.name} ($planTypeStr - $${sub.price})")
            } else {
                activities.add("اشتراك جديد: $planTypeStr - $${sub.price}")
            }
        }
        payments.firstOrNull()?.let { pay ->
            val payMember = members.find { m -> m.id == pay.memberId }
            if (payMember != null) {
                activities.add("دفعة جديدة: ${payMember.name} ($${pay.amount})")
            } else {
                activities.add("دفعة جديدة: $${pay.amount}")
            }
        }

        DashboardUiState(
            totalMembers = stats.totalMembers,
            activeMembers = stats.activeMembers,
            todayCheckIns = stats.todayAttendanceCount,
            currentlyInside = stats.currentlyInsideCount,
            sessionsUsedToday = sessionsUsedCount,
            todayPay = stats.todayPaymentsTotal,
            expSoonCount = stats.expiringSoonCount,
            activeSubsCount = stats.activeSubsCount,
            upcomingSubsCount = stats.upcomingSubsCount,
            expiredSubsCount = stats.expiredSubsCount,
            recentActivities = activities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}