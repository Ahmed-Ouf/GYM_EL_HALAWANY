package com.example.simplecalc.data.repository

import com.example.simplecalc.data.local.dao.AttendanceDao
import com.example.simplecalc.data.local.dao.AttendanceWithGames
import com.example.simplecalc.data.local.dao.GameDao
import com.example.simplecalc.data.local.dao.MemberDao
import com.example.simplecalc.data.local.dao.MemberMeasurementDao
import com.example.simplecalc.data.local.dao.PaymentDao
import com.example.simplecalc.data.local.dao.SubscriptionDao
import com.example.simplecalc.data.local.dao.TrainingScheduleDao
import com.example.simplecalc.data.local.dao.TrainingTypeDao
import com.example.simplecalc.data.local.entity.AttendanceEntity
import com.example.simplecalc.data.local.entity.AttendanceGameCrossRef
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.MemberMeasurementEntity
import com.example.simplecalc.data.local.entity.PaymentEntity
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.local.entity.TrainingScheduleEntity
import com.example.simplecalc.data.local.entity.TrainingTypeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

sealed class AddSubscriptionResult {
    data class Success(val subscriptionId: Long) : AddSubscriptionResult()
    data class OverlapError(val suggestedStartDateEpochDay: Long, val message: String) : AddSubscriptionResult()
}

interface MemberRepository {
    fun getAllActiveFlow(): Flow<List<MemberEntity>>
    fun getByIdFlow(id: Long): Flow<MemberEntity?>
    suspend fun getByIdDirect(id: Long): MemberEntity?
    suspend fun insert(member: MemberEntity): Long
    suspend fun update(member: MemberEntity)
    suspend fun archive(id: Long)
}

class MemberRepositoryImpl(private val memberDao: MemberDao) : MemberRepository {
    override fun getAllActiveFlow(): Flow<List<MemberEntity>> = memberDao.getAllActive()
    override fun getByIdFlow(id: Long): Flow<MemberEntity?> = memberDao.getById(id)
    override suspend fun getByIdDirect(id: Long): MemberEntity? = memberDao.getByIdDirect(id)
    override suspend fun insert(member: MemberEntity): Long = memberDao.insert(member)
    override suspend fun update(member: MemberEntity) { memberDao.update(member) }
    override suspend fun archive(id: Long) { memberDao.archive(id) }
}

interface GameRepository {
    fun getAllActiveFlow(): Flow<List<GameEntity>>
    fun getByIdFlow(id: Long): Flow<GameEntity?>
    suspend fun getByIdDirect(id: Long): GameEntity?
    fun getTrainingTypes(gameId: Long): Flow<List<TrainingTypeEntity>>
    suspend fun insertGame(game: GameEntity): Long
    suspend fun updateGame(game: GameEntity)
    suspend fun archiveGame(id: Long)
    suspend fun insertTrainingType(type: TrainingTypeEntity): Long
    suspend fun deleteTrainingType(type: TrainingTypeEntity)
}

class GameRepositoryImpl(
    private val gameDao: GameDao,
    private val trainingTypeDao: TrainingTypeDao
) : GameRepository {
    override fun getAllActiveFlow(): Flow<List<GameEntity>> = gameDao.getAllActive()
    override fun getByIdFlow(id: Long): Flow<GameEntity?> = gameDao.getById(id)
    override suspend fun getByIdDirect(id: Long): GameEntity? = gameDao.getByIdDirect(id)
    override fun getTrainingTypes(gameId: Long): Flow<List<TrainingTypeEntity>> = trainingTypeDao.getByGameId(gameId)
    override suspend fun insertGame(game: GameEntity): Long = gameDao.insert(game)
    override suspend fun updateGame(game: GameEntity) { gameDao.update(game) }
    override suspend fun archiveGame(id: Long) { gameDao.archive(id) }
    override suspend fun insertTrainingType(type: TrainingTypeEntity): Long = trainingTypeDao.insert(type)
    override suspend fun deleteTrainingType(type: TrainingTypeEntity) { trainingTypeDao.delete(type) }
}

interface SubscriptionRepository {
    fun getActiveForMemberFlow(memberId: Long, today: Long = LocalDate.now().toEpochDay()): Flow<List<SubscriptionEntity>>
    fun getAllForMemberFlow(memberId: Long): Flow<List<SubscriptionEntity>>
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>
    suspend fun addSubscription(subscription: SubscriptionEntity): AddSubscriptionResult
    suspend fun updateSubscription(subscription: SubscriptionEntity)
}

class SubscriptionRepositoryImpl(private val subscriptionDao: SubscriptionDao) : SubscriptionRepository {
    override fun getActiveForMemberFlow(memberId: Long, today: Long): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getActiveForMember(memberId, today)

    override fun getAllForMemberFlow(memberId: Long): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllForMember(memberId)

    override fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllSubscriptionsFlow()

    override suspend fun addSubscription(subscription: SubscriptionEntity): AddSubscriptionResult {
        val latest = subscriptionDao.getLatestForMemberAndGame(subscription.memberId, subscription.gameId)
        if (latest != null && subscription.startDate <= latest.endDate) {
            val suggestedStart = latest.endDate + 1
            return AddSubscriptionResult.OverlapError(
                suggestedStartDateEpochDay = suggestedStart,
                message = "تداخل مع اشتراك قائم. أقرب تاريخ بدء متاح هو يوم غد بعد الانتهاء."
            )
        }
        val newId = subscriptionDao.insert(subscription)
        return AddSubscriptionResult.Success(newId)
    }

    override suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.update(subscription)
    }
}

interface PaymentRepository {
    fun getBySubscriptionFlow(subId: Long): Flow<List<PaymentEntity>>
    fun getByMemberFlow(memberId: Long): Flow<List<PaymentEntity>>
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>
    fun getTotalForSubscriptionFlow(subId: Long): Flow<Double?>
    suspend fun recordPayment(payment: PaymentEntity): Long
}

class PaymentRepositoryImpl(private val paymentDao: PaymentDao) : PaymentRepository {
    override fun getBySubscriptionFlow(subId: Long): Flow<List<PaymentEntity>> = paymentDao.getBySubscription(subId)
    override fun getByMemberFlow(memberId: Long): Flow<List<PaymentEntity>> = paymentDao.getByMember(memberId)
    override fun getAllPaymentsFlow(): Flow<List<PaymentEntity>> = paymentDao.getAllPaymentsFlow()
    override fun getTotalForSubscriptionFlow(subId: Long): Flow<Double?> = paymentDao.getTotalForSubscription(subId)
    override suspend fun recordPayment(payment: PaymentEntity): Long = paymentDao.insert(payment)
}

interface AttendanceRepository {
    fun getTodayAttendanceWithGamesFlow(todayStartMillis: Long): Flow<List<AttendanceWithGames>>
    suspend fun getOpenAttendance(memberId: Long): AttendanceEntity?
    suspend fun checkIn(attendance: AttendanceEntity, gameIds: List<Long>): Result<Long>
    suspend fun checkOut(attendanceId: Long, checkOutTime: Long = System.currentTimeMillis())
}

class AttendanceRepositoryImpl(private val attendanceDao: AttendanceDao) : AttendanceRepository {
    override fun getTodayAttendanceWithGamesFlow(todayStartMillis: Long): Flow<List<AttendanceWithGames>> =
        attendanceDao.getTodayAttendanceWithGamesFlow(todayStartMillis)

    override suspend fun getOpenAttendance(memberId: Long): AttendanceEntity? =
        attendanceDao.getOpenForMember(memberId)

    override suspend fun checkIn(attendance: AttendanceEntity, gameIds: List<Long>): Result<Long> {
        val open = attendanceDao.getOpenForMember(attendance.memberId)
        if (open != null) {
            return Result.failure(IllegalStateException("العضو متواجد بالداخل حالياً."))
        }
        val attendanceId = attendanceDao.insert(attendance)
        val refs = gameIds.map { gameId -> AttendanceGameCrossRef(attendanceId, gameId) }
        attendanceDao.insertCrossRefs(refs)
        return Result.success(attendanceId)
    }

    override suspend fun checkOut(attendanceId: Long, checkOutTime: Long) {
        attendanceDao.updateCheckOut(attendanceId, checkOutTime)
    }
}

interface TrainingScheduleRepository {
    fun getForMemberAndDayFlow(memberId: Long, dayOfWeek: Int): Flow<List<TrainingScheduleEntity>>
    fun getForMemberFlow(memberId: Long): Flow<List<TrainingScheduleEntity>>
    suspend fun saveScheduleForMemberAndGame(memberId: Long, gameId: Long, trainingTypeIds: List<Long?>, dayOfWeek: Int)
}

class TrainingScheduleRepositoryImpl(private val trainingScheduleDao: TrainingScheduleDao) : TrainingScheduleRepository {
    override fun getForMemberAndDayFlow(memberId: Long, dayOfWeek: Int): Flow<List<TrainingScheduleEntity>> =
        trainingScheduleDao.getForMemberAndDay(memberId, dayOfWeek)

    override fun getForMemberFlow(memberId: Long): Flow<List<TrainingScheduleEntity>> =
        trainingScheduleDao.getForMember(memberId)

    override suspend fun saveScheduleForMemberAndGame(
        memberId: Long,
        gameId: Long,
        trainingTypeIds: List<Long?>,
        dayOfWeek: Int
    ) {
        // Delete old schedule for this member, game, and day
        trainingScheduleDao.deleteForMemberAndGame(memberId, gameId)
        val entities = trainingTypeIds.map { typeId ->
            TrainingScheduleEntity(
                memberId = memberId,
                gameId = gameId,
                trainingTypeId = typeId,
                dayOfWeek = dayOfWeek
            )
        }
        trainingScheduleDao.insertAll(entities)
    }
}

interface MemberMeasurementRepository {
    fun getForMemberFlow(memberId: Long): Flow<List<MemberMeasurementEntity>>
    suspend fun addMeasurement(measurement: MemberMeasurementEntity): Long
}

class MemberMeasurementRepositoryImpl(private val memberMeasurementDao: MemberMeasurementDao) : MemberMeasurementRepository {
    override fun getForMemberFlow(memberId: Long): Flow<List<MemberMeasurementEntity>> =
        memberMeasurementDao.getForMember(memberId)

    override suspend fun addMeasurement(measurement: MemberMeasurementEntity): Long =
        memberMeasurementDao.insert(measurement)
}