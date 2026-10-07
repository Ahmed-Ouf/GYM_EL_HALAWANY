package com.example.simplecalc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.simplecalc.data.local.converter.Converters
import com.example.simplecalc.data.local.dao.AttendanceDao
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
import com.example.simplecalc.data.local.entity.PaymentMethod
import com.example.simplecalc.data.local.entity.PlanType
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.local.entity.TrainingScheduleEntity
import com.example.simplecalc.data.local.entity.TrainingTypeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Database(
    entities = [
        MemberEntity::class,
        GameEntity::class,
        TrainingTypeEntity::class,
        SubscriptionEntity::class,
        PaymentEntity::class,
        AttendanceEntity::class,
        AttendanceGameCrossRef::class,
        TrainingScheduleEntity::class,
        MemberMeasurementEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun gameDao(): GameDao
    abstract fun trainingTypeDao(): TrainingTypeDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun trainingScheduleDao(): TrainingScheduleDao
    abstract fun memberMeasurementDao(): MemberMeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulateData(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateData(db: GymDatabase) {
            // Games
            val game1Id = db.gameDao().insert(GameEntity(name = "رفع الأثقال", defaultPrice = 500.0))
            val game2Id = db.gameDao().insert(GameEntity(name = "سباحة", defaultPrice = 400.0))
            val game3Id = db.gameDao().insert(GameEntity(name = "ملاكمة", defaultPrice = 600.0))

            // Training Types
            val tt1 = db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game1Id, name = "صدر"))
            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game1Id, name = "أكتاف"))
            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game1Id, name = "ظهر"))
            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game1Id, name = "ذراعين"))
            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game1Id, name = "أرجل"))
            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game1Id, name = "بطن"))

            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game2Id, name = "سباحة حرة"))
            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game2Id, name = "سباحة الظهر"))

            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game3Id, name = "أساسيات"))
            db.trainingTypeDao().insert(TrainingTypeEntity(gameId = game3Id, name = "لياقة وتقوية"))

            // Prepopulate initial members
            val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val m1Id = db.memberDao().insert(
                MemberEntity(
                    name = "أحمد محمد",
                    phone = "01012345678",
                    gender = "ذكر",
                    joinDate = today - 30
                )
            )
            val m2Id = db.memberDao().insert(
                MemberEntity(
                    name = "سارة أحمد",
                    phone = "01198765432",
                    gender = "أنثى",
                    joinDate = today - 15
                )
            )

            // Initial Subscriptions
            val sub1Id = db.subscriptionDao().insert(
                SubscriptionEntity(
                    memberId = m1Id,
                    gameId = game1Id,
                    planType = PlanType.MONTHLY,
                    price = 500.0,
                    startDate = today - 10,
                    endDate = today + 20
                )
            )
            val sub2Id = db.subscriptionDao().insert(
                SubscriptionEntity(
                    memberId = m2Id,
                    gameId = game2Id,
                    planType = PlanType.MONTHLY,
                    price = 400.0,
                    startDate = today - 5,
                    endDate = today + 25
                )
            )

            // Initial Payments
            db.paymentDao().insert(
                PaymentEntity(
                    memberId = m1Id,
                    subscriptionId = sub1Id,
                    amount = 500.0,
                    method = PaymentMethod.CASH,
                    date = today - 10,
                    note = "دفعة كاملة"
                )
            )
            db.paymentDao().insert(
                PaymentEntity(
                    memberId = m2Id,
                    subscriptionId = sub2Id,
                    amount = 400.0,
                    method = PaymentMethod.CARD,
                    date = today - 5,
                    note = "دفعة بالبطاقة"
                )
            )

            // Initial Measurements
            db.memberMeasurementDao().insert(
                MemberMeasurementEntity(
                    memberId = m1Id,
                    date = today - 30,
                    weight = 80.0,
                    notes = "بداية الاشتراك"
                )
            )
            db.memberMeasurementDao().insert(
                MemberMeasurementEntity(
                    memberId = m1Id,
                    date = today - 5,
                    weight = 76.5,
                    notes = "تقدم ملحوظ"
                )
            )

            // Initial Schedules for Ahmed (Member 1)
            val dayOfWeekVal = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            db.trainingScheduleDao().insert(
                TrainingScheduleEntity(
                    memberId = m1Id,
                    gameId = game1Id,
                    trainingTypeId = tt1,
                    dayOfWeek = dayOfWeekVal
                )
            )
        }
    }
}