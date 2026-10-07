package com.example.simplecalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.PlanType
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.repository.AddSubscriptionResult
import com.example.simplecalc.data.repository.GameRepository
import com.example.simplecalc.data.repository.MemberRepository
import com.example.simplecalc.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class SubscriptionsUiState(
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val games: List<GameEntity> = emptyList(),
    val selectedFilter: String = "الكل"
)

class SubscriptionsViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val memberRepository: MemberRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    val selectedFilter = MutableStateFlow("الكل")

    private val _addSubscriptionResult = MutableSharedFlow<AddSubscriptionResult>()
    val addSubscriptionResult: SharedFlow<AddSubscriptionResult> = _addSubscriptionResult.asSharedFlow()

    private val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())

    val uiState: StateFlow<SubscriptionsUiState> = combine(
        subscriptionRepository.getAllSubscriptionsFlow(),
        memberRepository.getAllActiveFlow(),
        gameRepository.getAllActiveFlow(),
        selectedFilter
    ) { subs, members, games, filter ->
        val filtered = when (filter) {
            "نشط" -> subs.filter { it.startDate <= todayEpochDay && it.endDate >= todayEpochDay }
            "منتهي" -> subs.filter { it.endDate < todayEpochDay }
            "قادم" -> subs.filter { it.startDate > todayEpochDay }
            else -> subs
        }
        SubscriptionsUiState(
            subscriptions = filtered,
            members = members,
            games = games,
            selectedFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionsUiState()
    )

    fun onFilterSelected(filter: String) {
        selectedFilter.value = filter
    }

    fun addSubscription(
        memberId: Long,
        gameId: Long,
        planType: PlanType,
        price: Double,
        startDateEpochDay: Long,
        endDateEpochDay: Long
    ) {
        viewModelScope.launch {
            val sub = SubscriptionEntity(
                memberId = memberId,
                gameId = gameId,
                planType = planType,
                price = price,
                startDate = startDateEpochDay,
                endDate = endDateEpochDay
            )
            val result = subscriptionRepository.addSubscription(sub)
            _addSubscriptionResult.emit(result)
        }
    }
}