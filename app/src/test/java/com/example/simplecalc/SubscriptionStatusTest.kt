package com.example.simplecalc

import com.example.simplecalc.data.repository.SubscriptionStatus
import com.example.simplecalc.data.repository.computeSubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionStatusTest {

    private val today: Long = 100L

    @Test
    fun testActiveSubscription() {
        val status = computeSubscriptionStatus(startDate = 80L, endDate = 120L, today = today)
        assertEquals(SubscriptionStatus.ACTIVE, status)
    }

    @Test
    fun testExpiredSubscription() {
        val status = computeSubscriptionStatus(startDate = 50L, endDate = 99L, today = today)
        assertEquals(SubscriptionStatus.EXPIRED, status)
    }

    @Test
    fun testUpcomingSubscription() {
        val status = computeSubscriptionStatus(startDate = 101L, endDate = 130L, today = today)
        assertEquals(SubscriptionStatus.UPCOMING, status)
    }

    @Test
    fun testExpiringSoonSubscription() {
        // Ending in 3 days (103 - 100 = 3)
        val status = computeSubscriptionStatus(startDate = 80L, endDate = 103L, today = today)
        assertEquals(SubscriptionStatus.EXPIRING_SOON, status)
    }

    @Test
    fun testExpiringTodaySubscription() {
        // Ending today (100 - 100 = 0)
        val status = computeSubscriptionStatus(startDate = 80L, endDate = 100L, today = today)
        assertEquals(SubscriptionStatus.EXPIRING_SOON, status)
    }
}
