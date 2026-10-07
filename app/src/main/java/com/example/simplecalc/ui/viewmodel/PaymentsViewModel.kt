package com.example.simplecalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.PaymentEntity
import com.example.simplecalc.data.local.entity.PaymentMethod
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.repository.MemberRepository
import com.example.simplecalc.data.repository.PaymentRepository
import com.example.simplecalc.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class PaymentsUiState(
    val payments: List<PaymentEntity> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val selectedFilter: String = "الكل",
    val totalAmount: Double = 0.0,
    val paymentCount: Int = 0
)

class PaymentsViewModel(
    private val paymentRepository: PaymentRepository,
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val selectedFilter = MutableStateFlow("الكل")

    val uiState: StateFlow<PaymentsUiState> = combine(
        paymentRepository.getAllPaymentsFlow(),
        memberRepository.getAllActiveFlow(),
        subscriptionRepository.getAllSubscriptionsFlow(),
        selectedFilter
    ) { payments, members, subs, filter ->
        val filtered = when (filter) {
            "نقدي" -> payments.filter { it.method == PaymentMethod.CASH }
            "بطاقة" -> payments.filter { it.method == PaymentMethod.CARD }
            "تحويل بنكي" -> payments.filter { it.method == PaymentMethod.TRANSFER }
            else -> payments
        }
        PaymentsUiState(
            payments = filtered,
            members = members,
            subscriptions = subs,
            selectedFilter = filter,
            totalAmount = filtered.sumOf { it.amount },
            paymentCount = filtered.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaymentsUiState()
    )

    fun onFilterSelected(filter: String) {
        selectedFilter.value = filter
    }

    fun recordPayment(
        memberId: Long,
        subscriptionId: Long,
        amount: Double,
        method: PaymentMethod,
        dateEpochDay: Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
        note: String? = null
    ) {
        viewModelScope.launch {
            paymentRepository.recordPayment(
                PaymentEntity(
                    memberId = memberId,
                    subscriptionId = subscriptionId,
                    amount = amount,
                    method = method,
                    date = dateEpochDay,
                    note = note
                )
            )
        }
    }
}