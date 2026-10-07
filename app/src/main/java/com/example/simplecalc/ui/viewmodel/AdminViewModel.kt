package com.example.simplecalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.TrainingTypeEntity
import com.example.simplecalc.data.repository.GameRepository
import com.example.simplecalc.data.repository.PaymentRepository
import com.example.simplecalc.data.repository.TrainingScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val gameRepository: GameRepository,
    private val paymentRepository: PaymentRepository,
    private val scheduleRepository: TrainingScheduleRepository
) : ViewModel() {

    val gamesState: StateFlow<List<GameEntity>> = gameRepository.getAllActiveFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getTrainingTypesForGame(gameId: Long): Flow<List<TrainingTypeEntity>> =
        gameRepository.getTrainingTypes(gameId)

    fun addGame(name: String, defaultPrice: Double) {
        viewModelScope.launch {
            gameRepository.insertGame(
                GameEntity(name = name, defaultPrice = defaultPrice)
            )
        }
    }

    fun updateGame(game: GameEntity) {
        viewModelScope.launch {
            gameRepository.updateGame(game)
        }
    }

    fun archiveGame(id: Long) {
        viewModelScope.launch {
            gameRepository.archiveGame(id)
        }
    }

    fun addTrainingType(gameId: Long, name: String) {
        viewModelScope.launch {
            gameRepository.insertTrainingType(
                TrainingTypeEntity(gameId = gameId, name = name)
            )
        }
    }

    fun deleteTrainingType(trainingType: TrainingTypeEntity) {
        viewModelScope.launch {
            gameRepository.deleteTrainingType(trainingType)
        }
    }

    fun saveTrainingSchedule(memberId: Long, gameId: Long, trainingTypeIds: List<Long?>, dayOfWeek: Int) {
        viewModelScope.launch {
            scheduleRepository.saveScheduleForMemberAndGame(memberId, gameId, trainingTypeIds, dayOfWeek)
        }
    }
}