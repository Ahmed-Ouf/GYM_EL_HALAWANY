package com.example.simplecalc.data.local.converter

import androidx.room.TypeConverter
import com.example.simplecalc.data.local.entity.PaymentMethod
import com.example.simplecalc.data.local.entity.PlanType
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromPlanType(value: String?): PlanType? {
        return value?.let {
            try {
                PlanType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun toPlanType(planType: PlanType?): String? {
        return planType?.name
    }

    @TypeConverter
    fun fromPaymentMethod(value: String?): PaymentMethod? {
        return value?.let {
            try {
                PaymentMethod.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun toPaymentMethod(paymentMethod: PaymentMethod?): String? {
        return paymentMethod?.name
    }
}