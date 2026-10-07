package com.example.simplecalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.MemberMeasurementEntity
import com.example.simplecalc.data.repository.AttendanceRepository
import com.example.simplecalc.data.repository.MemberMeasurementRepository
import com.example.simplecalc.data.repository.MemberRepository
import com.example.simplecalc.data.repository.SubscriptionRepository
import com.example.simplecalc.data.repository.TrainingScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class MembersUiState(
    val members: List<MemberEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class MembersViewModel(
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val scheduleRepository: TrainingScheduleRepository,
    private val measurementRepository: MemberMeasurementRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<MembersUiState> = combine(
        memberRepository.getAllActiveFlow(),
        searchQuery
    ) { members, query ->
        val filtered = if (query.isBlank()) {
            members
        } else {
            members.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true)
            }
        }
        MembersUiState(members = filtered, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MembersUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun addMember(name: String, phone: String, gender: String, birthDate: Long?, joinDate: Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()), notes: String? = null) {
        viewModelScope.launch {
            memberRepository.insert(
                MemberEntity(
                    name = name,
                    phone = phone,
                    gender = gender,
                    birthDate = birthDate,
                    joinDate = joinDate,
                    notes = notes
                )
            )
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            memberRepository.update(member)
        }
    }

    fun archiveMember(id: Long) {
        viewModelScope.launch {
            memberRepository.archive(id)
        }
    }

    fun getMemberSubscriptionsFlow(memberId: Long) = subscriptionRepository.getAllForMemberFlow(memberId)

    fun getMemberMeasurementsFlow(memberId: Long) = measurementRepository.getForMemberFlow(memberId)

    fun getMemberSchedulesFlow(memberId: Long) = scheduleRepository.getForMemberFlow(memberId)

    fun addMeasurement(memberId: Long, weight: Double, notes: String?, date: Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())) {
        viewModelScope.launch {
            measurementRepository.addMeasurement(
                MemberMeasurementEntity(
                    memberId = memberId,
                    date = date,
                    weight = weight,
                    notes = notes
                )
            )
        }
    }
}