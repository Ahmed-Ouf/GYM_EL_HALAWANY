package com.example.simplecalc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val gender: String,
    val birthDate: Long? = null,
    val joinDate: Long,
    val notes: String? = null,
    val isArchived: Boolean = false
)

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val defaultPrice: Double,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "training_types",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class TrainingTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val name: String
)

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("memberId"), Index("gameId")]
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val gameId: Long,
    val planType: PlanType,
    val price: Double,
    val startDate: Long,
    val endDate: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("memberId"), Index("subscriptionId")]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val subscriptionId: Long,
    val amount: Double,
    val method: PaymentMethod,
    val date: Long,
    val note: String? = null
)

@Entity(
    tableName = "attendances",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val checkIn: Long,
    val checkOut: Long? = null
)

@Entity(
    tableName = "attendance_game_cross_ref",
    primaryKeys = ["attendanceId", "gameId"],
    foreignKeys = [
        ForeignKey(
            entity = AttendanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["attendanceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("attendanceId"), Index("gameId")]
)
data class AttendanceGameCrossRef(
    val attendanceId: Long,
    val gameId: Long
)

@Entity(
    tableName = "training_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TrainingTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingTypeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("memberId"), Index("gameId"), Index("trainingTypeId")]
)
data class TrainingScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val gameId: Long,
    val trainingTypeId: Long? = null,
    val dayOfWeek: Int
)

@Entity(
    tableName = "member_measurements",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class MemberMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val date: Long,
    val weight: Double,
    val notes: String? = null
)