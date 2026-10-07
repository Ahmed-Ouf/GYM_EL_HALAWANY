package com.example.simplecalc.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
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

data class AttendanceWithGames(
    @Embedded val attendance: AttendanceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = AttendanceGameCrossRef::class,
            parentColumn = "attendanceId",
            entityColumn = "gameId"
        )
    )
    val games: List<GameEntity>
)

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity): Int

    @Query("SELECT * FROM members WHERE id = :id")
    fun getById(id: Long): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getByIdDirect(id: Long): MemberEntity?

    @Query("SELECT * FROM members WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllActive(): Flow<List<MemberEntity>>

    @Query("UPDATE members SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long): Int
}

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity): Int

    @Query("SELECT * FROM games WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllActive(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getById(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getByIdDirect(id: Long): GameEntity?

    @Query("UPDATE games SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long): Int
}

@Dao
interface TrainingTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trainingType: TrainingTypeEntity): Long

    @Query("SELECT * FROM training_types WHERE gameId = :gameId ORDER BY id ASC")
    fun getByGameId(gameId: Long): Flow<List<TrainingTypeEntity>>

    @Delete
    suspend fun delete(trainingType: TrainingTypeEntity): Int
}

@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity): Int

    @Query("SELECT * FROM subscriptions WHERE memberId = :memberId AND startDate <= :today AND endDate >= :today ORDER BY endDate DESC")
    fun getActiveForMember(memberId: Long, today: Long): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE memberId = :memberId ORDER BY startDate DESC")
    fun getAllForMember(memberId: Long): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE memberId = :memberId AND gameId = :gameId ORDER BY endDate DESC LIMIT 1")
    suspend fun getLatestForMemberAndGame(memberId: Long, gameId: Long): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions ORDER BY id DESC")
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>
}

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE subscriptionId = :subId ORDER BY date DESC")
    fun getBySubscription(subId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE memberId = :memberId ORDER BY date DESC")
    fun getByMember(memberId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT SUM(amount) FROM payments WHERE subscriptionId = :subId")
    fun getTotalForSubscription(subId: Long): Flow<Double?>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>
}

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: AttendanceGameCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<AttendanceGameCrossRef>): List<Long>

    @Query("UPDATE attendances SET checkOut = :checkOut WHERE id = :id")
    suspend fun updateCheckOut(id: Long, checkOut: Long): Int

    @Query("SELECT * FROM attendances WHERE memberId = :memberId AND checkOut IS NULL LIMIT 1")
    suspend fun getOpenForMember(memberId: Long): AttendanceEntity?

    @Query("SELECT * FROM attendances WHERE checkIn >= :todayStartMillis ORDER BY checkIn DESC")
    fun getTodayAttendanceFlow(todayStartMillis: Long): Flow<List<AttendanceEntity>>

    @Transaction
    @Query("SELECT * FROM attendances WHERE id = :id")
    fun getAttendanceWithGames(id: Long): Flow<AttendanceWithGames?>

    @Transaction
    @Query("SELECT * FROM attendances WHERE checkIn >= :todayStartMillis ORDER BY checkIn DESC")
    fun getTodayAttendanceWithGamesFlow(todayStartMillis: Long): Flow<List<AttendanceWithGames>>
}

@Dao
interface TrainingScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: TrainingScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<TrainingScheduleEntity>): List<Long>

    @Query("DELETE FROM training_schedules WHERE memberId = :memberId AND gameId = :gameId")
    suspend fun deleteForMemberAndGame(memberId: Long, gameId: Long): Int

    @Query("SELECT * FROM training_schedules WHERE memberId = :memberId AND dayOfWeek = :dayOfWeek")
    fun getForMemberAndDay(memberId: Long, dayOfWeek: Int): Flow<List<TrainingScheduleEntity>>

    @Query("SELECT * FROM training_schedules WHERE memberId = :memberId")
    fun getForMember(memberId: Long): Flow<List<TrainingScheduleEntity>>
}

@Dao
interface MemberMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MemberMeasurementEntity): Long

    @Query("SELECT * FROM member_measurements WHERE memberId = :memberId ORDER BY date ASC")
    fun getForMember(memberId: Long): Flow<List<MemberMeasurementEntity>>
}