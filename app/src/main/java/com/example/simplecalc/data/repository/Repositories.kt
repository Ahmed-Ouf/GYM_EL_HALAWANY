package com.example.simplecalc.data.repository

import com.example.simplecalc.data.local.dao.AttendanceDao
import com.example.simplecalc.data.local.dao.AttendanceWithGames
import com.example.simplecalc.data.local.dao.DashboardDao
import com.example.simplecalc.data.local.dao.GameDao
import com.example.simplecalc.data.local.dao.MemberDao
import com.example.simplecalc.data.local.dao.MemberMeasurementDao
import com.example.simplecalc.data.local.dao.PaymentDao
import com.example.simplecalc.data.local.dao.PaymentWithMemberAndSubscription
import com.example.simplecalc.data.local.dao.ScheduleWithDetails
import com.example.simplecalc.data.local.dao.SubscriptionDao
import com.example.simplecalc.data.local.dao.SubscriptionWithMemberAndGame
import com.example.simplecalc.data.local.dao.TrainingScheduleDao
import com.example.simplecalc.data.local.dao.TrainingTypeDao
import com.example.simplecalc.data.local.entity.AttendanceEntity
import com.example.simplecalc.data.local.entity.AttendanceGameCrossRef
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.MemberMeasurementEntity
import com.example.simplecalc.data.local.entity.PaymentEntity
import com.example.simplecalc.data.local.entity.PaymentMethod
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.local.entity.TrainingScheduleEntity
import com.example.simplecalc.data.local.entity.TrainingTypeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    UPCOMING,
    EXPIRING_SOON
}

fun computeSubscriptionStatus(
    startDate: Long,
    endDate: Long,
    today: Long = LocalDate.now().toEpochDay()
): SubscriptionStatus {
    return when {
        endDate < today -> SubscriptionStatus.EXPIRED
        startDate > today -> SubscriptionStatus.UPCOMING
        (endDate - today) in 0..7 -> SubscriptionStatus.EXPIRING_SOON
        else -> SubscriptionStatus.ACTIVE
    }
}

sealed class OperationResult<out T> {
    data class Success<out T>(val data: T) : OperationResult<T>()
    data class OverlapError(val suggestedStartDateEpochDay: Long, val message: String) : OperationResult<Nothing>()
    data class ArchivedMemberError(val message: String = "العضو مؤرشف ولا يمكن إجراء هذه العملية له.") : OperationResult<Nothing>()
    data class Error(val message: String) : OperationResult<Nothing>()
}

typealias AddSubscriptionResult = OperationResult<Long>

interface MemberRepository {
    fun getAllActiveFlow(): Flow<List<MemberEntity>>
    fun searchMembersFlow(query: String): Flow<List<MemberEntity>>
    fun getByIdFlow(id: Long): Flow<MemberEntity?>
    suspend fun getByIdDirect(id: Long): MemberEntity?
    suspend fun insert(member: MemberEntity): Long
    suspend fun update(member: MemberEntity)
    suspend fun archive(id: Long)
}

class MemberRepositoryImpl(private val memberDao: MemberDao) : MemberRepository {
    override fun getAllActiveFlow(): Flow<List<MemberEntity>> = memberDao.getAllActive()
    override fun searchMembersFlow(query: String): Flow<List<MemberEntity>> = memberDao.searchMembers(query)
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
    fun getAllSubscriptionsWithMemberAndGameFlow(): Flow<List<SubscriptionWithMemberAndGame>>
    suspend fun addSubscription(subscription: SubscriptionEntity): OperationResult<Long>
    suspend fun updateSubscription(subscription: SubscriptionEntity)
}

class SubscriptionRepositoryImpl(
    private val subscriptionDao: SubscriptionDao,
    private val memberDao: MemberDao
) : SubscriptionRepository {
    override fun getActiveForMemberFlow(memberId: Long, today: Long): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getActiveForMember(memberId, today)

    override fun getAllForMemberFlow(memberId: Long): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllForMember(memberId)

    override fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllSubscriptionsFlow()

    override fun getAllSubscriptionsWithMemberAndGameFlow(): Flow<List<SubscriptionWithMemberAndGame>> =
        subscriptionDao.getAllSubscriptionsWithMemberAndGame()

    override suspend fun addSubscription(subscription: SubscriptionEntity): OperationResult<Long> {
        val member = memberDao.getByIdDirect(subscription.memberId)
        if (member == null || member.isArchived) {
            return OperationResult.ArchivedMemberError("لا يمكن إضافة اشتراك لعضو مؤرشف.")
        }

        val overlaps = subscriptionDao.checkOverlap(
            memberId = subscription.memberId,
            gameId = subscription.gameId,
            startDate = subscription.startDate,
            endDate = subscription.endDate
        )
        if (overlaps > 0) {
            val latest = subscriptionDao.getLatestForMemberAndGame(subscription.memberId, subscription.gameId)
            val suggestedStart = (latest?.endDate ?: subscription.startDate) + 1
            return OperationResult.OverlapError(
                suggestedStartDateEpochDay = suggestedStart,
                message = "تداخل مع اشتراك قائم. أقرب تاريخ بدء متاح هو يوم غد بعد الانتهاء."
            )
        }
        val newId = subscriptionDao.insert(subscription)
        return OperationResult.Success(newId)
    }

    override suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.update(subscription)
    }
}

interface PaymentRepository {
    fun getBySubscriptionFlow(subId: Long): Flow<List<PaymentEntity>>
    fun getByMemberFlow(memberId: Long): Flow<List<PaymentEntity>>
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>
    fun getPaymentsFilteredFlow(method: PaymentMethod?): Flow<List<PaymentEntity>>
    fun getPaymentsWithDetailsFilteredFlow(method: PaymentMethod?): Flow<List<PaymentWithMemberAndSubscription>>
    fun getTotalPaymentsFlow(method: PaymentMethod? = null): Flow<Double>
    fun getTotalForSubscriptionFlow(subId: Long): Flow<Double?>
    suspend fun recordPayment(payment: PaymentEntity): OperationResult<Long>
}

class PaymentRepositoryImpl(
    private val paymentDao: PaymentDao,
    private val memberDao: MemberDao
) : PaymentRepository {
    override fun getBySubscriptionFlow(subId: Long): Flow<List<PaymentEntity>> = paymentDao.getBySubscription(subId)
    override fun getByMemberFlow(memberId: Long): Flow<List<PaymentEntity>> = paymentDao.getByMember(memberId)
    override fun getAllPaymentsFlow(): Flow<List<PaymentEntity>> = paymentDao.getAllPaymentsFlow()
    override fun getPaymentsFilteredFlow(method: PaymentMethod?): Flow<List<PaymentEntity>> = paymentDao.getPaymentsFiltered(method)
    override fun getPaymentsWithDetailsFilteredFlow(method: PaymentMethod?): Flow<List<PaymentWithMemberAndSubscription>> = paymentDao.getPaymentsWithDetailsFiltered(method)
    override fun getTotalPaymentsFlow(method: PaymentMethod?): Flow<Double> = paymentDao.getTotalPayments(method)
    override fun getTotalForSubscriptionFlow(subId: Long): Flow<Double?> = paymentDao.getTotalForSubscription(subId)

    override suspend fun recordPayment(payment: PaymentEntity): OperationResult<Long> {
        val member = memberDao.getByIdDirect(payment.memberId)
        if (member == null || member.isArchived) {
            return OperationResult.ArchivedMemberError("لا يمكن تسجيل دفعة لعضو مؤرشف.")
        }
        val id = paymentDao.insert(payment)
        return OperationResult.Success(id)
    }
}

interface AttendanceRepository {
    fun getTodayAttendanceWithGamesFlow(todayStartMillis: Long): Flow<List<AttendanceWithGames>>
    suspend fun getOpenAttendance(memberId: Long): AttendanceEntity?
    suspend fun checkIn(attendance: AttendanceEntity, gameIds: List<Long>): OperationResult<Long>
    suspend fun checkOut(attendanceId: Long, checkOutTime: Long = System.currentTimeMillis())
}

class AttendanceRepositoryImpl(
    private val attendanceDao: AttendanceDao,
    private val memberDao: MemberDao
) : AttendanceRepository {
    override fun getTodayAttendanceWithGamesFlow(todayStartMillis: Long): Flow<List<AttendanceWithGames>> =
        attendanceDao.getTodayAttendanceWithGamesFlow(todayStartMillis)

    override suspend fun getOpenAttendance(memberId: Long): AttendanceEntity? =
        attendanceDao.getOpenForMember(memberId)

    override suspend fun checkIn(attendance: AttendanceEntity, gameIds: List<Long>): OperationResult<Long> {
        val member = memberDao.getByIdDirect(attendance.memberId)
        if (member == null || member.isArchived) {
            return OperationResult.ArchivedMemberError("لا يمكن تسجيل حضور لعضو مؤرشف.")
        }
        val open = attendanceDao.getOpenForMember(attendance.memberId)
        if (open != null) {
            return OperationResult.Error("العضو متواجد بالداخل حالياً.")
        }
        val attendanceId = attendanceDao.insert(attendance)
        val refs = gameIds.map { gameId -> AttendanceGameCrossRef(attendanceId, gameId) }
        attendanceDao.insertCrossRefs(refs)
        return OperationResult.Success(attendanceId)
    }

    override suspend fun checkOut(attendanceId: Long, checkOutTime: Long) {
        attendanceDao.updateCheckOut(attendanceId, checkOutTime)
    }
}

interface TrainingScheduleRepository {
    fun getForMemberAndDayFlow(memberId: Long, dayOfWeek: Int): Flow<List<TrainingScheduleEntity>>
    fun getScheduleWithDetailsForMemberAndDayFlow(memberId: Long, dayOfWeek: Int): Flow<List<ScheduleWithDetails>>
    fun getForMemberFlow(memberId: Long): Flow<List<TrainingScheduleEntity>>
    suspend fun saveScheduleForMemberAndGame(memberId: Long, gameId: Long, trainingTypeIds: List<Long?>, dayOfWeek: Int)
}

class TrainingScheduleRepositoryImpl(private val trainingScheduleDao: TrainingScheduleDao) : TrainingScheduleRepository {
    override fun getForMemberAndDayFlow(memberId: Long, dayOfWeek: Int): Flow<List<TrainingScheduleEntity>> =
        trainingScheduleDao.getForMemberAndDay(memberId, dayOfWeek)

    override fun getScheduleWithDetailsForMemberAndDayFlow(memberId: Long, dayOfWeek: Int): Flow<List<ScheduleWithDetails>> =
        trainingScheduleDao.getScheduleWithDetailsForMemberAndDay(memberId, dayOfWeek)

    override fun getForMemberFlow(memberId: Long): Flow<List<TrainingScheduleEntity>> =
        trainingScheduleDao.getForMember(memberId)

    override suspend fun saveScheduleForMemberAndGame(
        memberId: Long,
        gameId: Long,
        trainingTypeIds: List<Long?>,
        dayOfWeek: Int
    ) {
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

data class DashboardStats(
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val todayAttendanceCount: Int = 0,
    val currentlyInsideCount: Int = 0,
    val todayPaymentsTotal: Double = 0.0,
    val expiringSoonCount: Int = 0,
    val activeSubsCount: Int = 0,
    val upcomingSubsCount: Int = 0,
    val expiredSubsCount: Int = 0
)

interface DashboardRepository {
    fun getDashboardStatsFlow(
        todayEpochDay: Long = LocalDate.now().toEpochDay(),
        todayStartMillis: Long = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000L))
    ): Flow<DashboardStats>
}

class DashboardRepositoryImpl(
    private val dashboardDao: DashboardDao,
    private val subscriptionDao: SubscriptionDao,
    private val attendanceDao: AttendanceDao
) : DashboardRepository {
    override fun getDashboardStatsFlow(
        todayEpochDay: Long,
        todayStartMillis: Long
    ): Flow<DashboardStats> {
        val todayEndMillis = todayStartMillis + (24 * 60 * 60 * 1000L) - 1
        return combine(
            dashboardDao.getTotalMembersCount(),
            dashboardDao.getActiveMembersCount(todayEpochDay),
            dashboardDao.getTodayAttendanceCount(todayStartMillis, todayEndMillis),
            dashboardDao.getTodayPaymentsTotal(todayEpochDay),
            dashboardDao.getSubscriptionsExpiringInNext7DaysCount(todayEpochDay),
            subscriptionDao.getAllSubscriptionsFlow(),
            attendanceDao.getTodayAttendanceFlow(todayStartMillis)
        ) { flows ->
            val totalMembers = flows[0] as Int
            val activeMembers = flows[1] as Int
            val todayAttendance = flows[2] as Int
            val todayPay = flows[3] as Double
            val expiringSoon = flows[4] as Int
            @Suppress("UNCHECKED_CAST")
            val allSubs = flows[5] as List<SubscriptionEntity>
            @Suppress("UNCHECKED_CAST")
            val todayAttendanceList = flows[6] as List<AttendanceEntity>

            val activeSubsCount = allSubs.count { it.startDate <= todayEpochDay && it.endDate >= todayEpochDay }
            val upcomingSubsCount = allSubs.count { it.startDate > todayEpochDay }
            val expiredSubsCount = allSubs.count { it.endDate < todayEpochDay }
            val currentlyInside = todayAttendanceList.count { it.checkOut == null }

            DashboardStats(
                totalMembers = totalMembers,
                activeMembers = activeMembers,
                todayAttendanceCount = todayAttendance,
                currentlyInsideCount = currentlyInside,
                todayPaymentsTotal = todayPay,
                expiringSoonCount = expiringSoon,
                activeSubsCount = activeSubsCount,
                upcomingSubsCount = upcomingSubsCount,
                expiredSubsCount = expiredSubsCount
            )
        }
    }
}