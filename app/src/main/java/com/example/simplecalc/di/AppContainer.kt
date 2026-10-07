package com.example.simplecalc.di

import android.content.Context
import com.example.simplecalc.data.local.GymDatabase
import com.example.simplecalc.data.local.dao.AttendanceWithGames
import com.example.simplecalc.data.local.entity.AttendanceEntity
import com.example.simplecalc.data.local.entity.GameEntity
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.MemberMeasurementEntity
import com.example.simplecalc.data.local.entity.PaymentEntity
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.local.entity.TrainingScheduleEntity
import com.example.simplecalc.data.local.entity.TrainingTypeEntity
import com.example.simplecalc.data.repository.AddSubscriptionResult
import com.example.simplecalc.data.repository.AttendanceRepository
import com.example.simplecalc.data.repository.AttendanceRepositoryImpl
import com.example.simplecalc.data.repository.GameRepository
import com.example.simplecalc.data.repository.GameRepositoryImpl
import com.example.simplecalc.data.repository.MemberMeasurementRepository
import com.example.simplecalc.data.repository.MemberMeasurementRepositoryImpl
import com.example.simplecalc.data.repository.MemberRepository
import com.example.simplecalc.data.repository.MemberRepositoryImpl
import com.example.simplecalc.data.repository.PaymentRepository
import com.example.simplecalc.data.repository.PaymentRepositoryImpl
import com.example.simplecalc.data.repository.SubscriptionRepository
import com.example.simplecalc.data.repository.SubscriptionRepositoryImpl
import com.example.simplecalc.data.repository.TrainingScheduleRepository
import com.example.simplecalc.data.repository.TrainingScheduleRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AppContainer {
    val memberRepository: MemberRepository
    val gameRepository: GameRepository
    val subscriptionRepository: SubscriptionRepository
    val paymentRepository: PaymentRepository
    val attendanceRepository: AttendanceRepository
    val trainingScheduleRepository: TrainingScheduleRepository
    val memberMeasurementRepository: MemberMeasurementRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val db: GymDatabase by lazy {
        GymDatabase.getDatabase(context)
    }

    override val memberRepository: MemberRepository by lazy {
        MemberRepositoryImpl(db.memberDao())
    }

    override val gameRepository: GameRepository by lazy {
        GameRepositoryImpl(db.gameDao(), db.trainingTypeDao())
    }

    override val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepositoryImpl(db.subscriptionDao())
    }

    override val paymentRepository: PaymentRepository by lazy {
        PaymentRepositoryImpl(db.paymentDao())
    }

    override val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(db.attendanceDao())
    }

    override val trainingScheduleRepository: TrainingScheduleRepository by lazy {
        TrainingScheduleRepositoryImpl(db.trainingScheduleDao())
    }

    override val memberMeasurementRepository: MemberMeasurementRepository by lazy {
        MemberMeasurementRepositoryImpl(db.memberMeasurementDao())
    }
}

class PreviewAppContainer : AppContainer {
    override val memberRepository: MemberRepository = FakeMemberRepository()
    override val gameRepository: GameRepository = FakeGameRepository()
    override val subscriptionRepository: SubscriptionRepository = FakeSubscriptionRepository()
    override val paymentRepository: PaymentRepository = FakePaymentRepository()
    override val attendanceRepository: AttendanceRepository = FakeAttendanceRepository()
    override val trainingScheduleRepository: TrainingScheduleRepository = FakeTrainingScheduleRepository()
    override val memberMeasurementRepository: MemberMeasurementRepository = FakeMemberMeasurementRepository()
}

class FakeMemberRepository : MemberRepository {
    override fun getAllActiveFlow(): Flow<List<MemberEntity>> = flowOf(emptyList())
    override fun getByIdFlow(id: Long): Flow<MemberEntity?> = flowOf(null)
    override suspend fun getByIdDirect(id: Long): MemberEntity? = null
    override suspend fun insert(member: MemberEntity): Long = 1L
    override suspend fun update(member: MemberEntity) {}
    override suspend fun archive(id: Long) {}
}

class FakeGameRepository : GameRepository {
    override fun getAllActiveFlow(): Flow<List<GameEntity>> = flowOf(emptyList())
    override fun getByIdFlow(id: Long): Flow<GameEntity?> = flowOf(null)
    override suspend fun getByIdDirect(id: Long): GameEntity? = null
    override fun getTrainingTypes(gameId: Long): Flow<List<TrainingTypeEntity>> = flowOf(emptyList())
    override suspend fun insertGame(game: GameEntity): Long = 1L
    override suspend fun updateGame(game: GameEntity) {}
    override suspend fun archiveGame(id: Long) {}
    override suspend fun insertTrainingType(type: TrainingTypeEntity): Long = 1L
    override suspend fun deleteTrainingType(type: TrainingTypeEntity) {}
}

class FakeSubscriptionRepository : SubscriptionRepository {
    override fun getActiveForMemberFlow(memberId: Long, today: Long): Flow<List<SubscriptionEntity>> =
        flowOf(emptyList())
    override fun getAllForMemberFlow(memberId: Long): Flow<List<SubscriptionEntity>> = flowOf(emptyList())
    override fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>> = flowOf(emptyList())
    override suspend fun addSubscription(subscription: SubscriptionEntity): AddSubscriptionResult = AddSubscriptionResult.Success(1L)
    override suspend fun updateSubscription(subscription: SubscriptionEntity) {}
}

class FakePaymentRepository : PaymentRepository {
    override fun getBySubscriptionFlow(subId: Long): Flow<List<PaymentEntity>> = flowOf(emptyList())
    override fun getByMemberFlow(memberId: Long): Flow<List<PaymentEntity>> = flowOf(emptyList())
    override fun getAllPaymentsFlow(): Flow<List<PaymentEntity>> = flowOf(emptyList())
    override fun getTotalForSubscriptionFlow(subId: Long): Flow<Double?> = flowOf(0.0)
    override suspend fun recordPayment(payment: PaymentEntity): Long = 1L
}

class FakeAttendanceRepository : AttendanceRepository {
    override fun getTodayAttendanceWithGamesFlow(todayStartMillis: Long): Flow<List<AttendanceWithGames>> =
        flowOf(emptyList())
    override suspend fun getOpenAttendance(memberId: Long): AttendanceEntity? = null
    override suspend fun checkIn(attendance: AttendanceEntity, gameIds: List<Long>): Result<Long> = Result.success(1L)
    override suspend fun checkOut(attendanceId: Long, checkOutTime: Long) {}
}

class FakeTrainingScheduleRepository : TrainingScheduleRepository {
    override fun getForMemberAndDayFlow(memberId: Long, dayOfWeek: Int): Flow<List<TrainingScheduleEntity>> =
        flowOf(emptyList())
    override fun getForMemberFlow(memberId: Long): Flow<List<TrainingScheduleEntity>> = flowOf(emptyList())
    override suspend fun saveScheduleForMemberAndGame(memberId: Long, gameId: Long, trainingTypeIds: List<Long?>, dayOfWeek: Int) {}
}

class FakeMemberMeasurementRepository : MemberMeasurementRepository {
    override fun getForMemberFlow(memberId: Long): Flow<List<MemberMeasurementEntity>> = flowOf(emptyList())
    override suspend fun addMeasurement(measurement: MemberMeasurementEntity): Long = 1L
}