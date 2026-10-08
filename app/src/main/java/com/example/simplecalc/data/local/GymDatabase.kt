package com.example.simplecalc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.simplecalc.data.local.converter.Converters
import com.example.simplecalc.data.local.dao.AttendanceDao
import com.example.simplecalc.data.local.dao.DashboardDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    exportSchema = true
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
    abstract fun dashboardDao(): DashboardDao

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
            // Games placeholder
            val weightliftingId = db.gameDao().insert(GameEntity(name = "رفع الأثقال", defaultPrice = 500.0))
            db.gameDao().insert(GameEntity(name = "السباحة", defaultPrice = 400.0))
            db.gameDao().insert(GameEntity(name = "الملاكمة", defaultPrice = 450.0))

            // Training Types for Weightlifting
            val weightliftingTypes = listOf("صدر", "أكتاف", "ظهر", "ذراعين", "أرجل", "بطن")
            weightliftingTypes.forEach { typeName ->
                db.trainingTypeDao().insert(TrainingTypeEntity(gameId = weightliftingId, name = typeName))
            }

            // Do NOT seed members, subscriptions, payments, measurements, or schedules.
        }
    }
}