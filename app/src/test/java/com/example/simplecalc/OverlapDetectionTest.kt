package com.example.simplecalc

import com.example.simplecalc.data.local.dao.MemberDao
import com.example.simplecalc.data.local.dao.SubscriptionDao
import com.example.simplecalc.data.local.dao.SubscriptionWithMemberAndGame
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.local.entity.PlanType
import com.example.simplecalc.data.local.entity.SubscriptionEntity
import com.example.simplecalc.data.repository.OperationResult
import com.example.simplecalc.data.repository.SubscriptionRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OverlapDetectionTest {

    private class FakeMemberDao : MemberDao {
        val members = mutableMapOf<Long, MemberEntity>()
        override suspend fun insert(member: MemberEntity): Long {
            val id = if (member.id == 0L) (members.size + 1).toLong() else member.id
            members[id] = member.copy(id = id)
            return id
        }
        override suspend fun update(member: MemberEntity): Int { members[member.id] = member; return 1 }
        override fun getById(id: Long): Flow<MemberEntity?> = flowOf(members[id])
        override suspend fun getByIdDirect(id: Long): MemberEntity? = members[id]
        override fun getAllActive(): Flow<List<MemberEntity>> = flowOf(members.values.filter { !it.isArchived })
        override fun searchMembers(query: String): Flow<List<MemberEntity>> = flowOf(members.values.filter { !it.isArchived })
        override suspend fun archive(id: Long): Int { members[id]?.let { members[id] = it.copy(isArchived = true) }; return 1 }
    }

    private class FakeSubscriptionDao : SubscriptionDao {
        val subscriptions = mutableListOf<SubscriptionEntity>()
        var nextId = 1L

        override suspend fun insert(subscription: SubscriptionEntity): Long {
            val entity = subscription.copy(id = nextId++)
            subscriptions.add(entity)
            return entity.id
        }

        override suspend fun update(subscription: SubscriptionEntity): Int = 1

        override fun getActiveForMember(memberId: Long, today: Long): Flow<List<SubscriptionEntity>> =
            flowOf(subscriptions.filter { it.memberId == memberId && it.startDate <= today && it.endDate >= today })

        override fun getActiveWithDetailsForMember(memberId: Long, today: Long) = flowOf(emptyList<SubscriptionWithMemberAndGame>())

        override fun getAllForMember(memberId: Long): Flow<List<SubscriptionEntity>> =
            flowOf(subscriptions.filter { it.memberId == memberId })

        override suspend fun getLatestForMemberAndGame(memberId: Long, gameId: Long): SubscriptionEntity? =
            subscriptions.filter { it.memberId == memberId && it.gameId == gameId }.maxByOrNull { it.endDate }

        override fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>> = flowOf(subscriptions)

        override fun getAllSubscriptionsWithMemberAndGame() = flowOf(emptyList<SubscriptionWithMemberAndGame>())

        override suspend fun checkOverlap(
            memberId: Long,
            gameId: Long,
            startDate: Long,
            endDate: Long,
            excludeId: Long?
        ): Int {
            return subscriptions.count { sub ->
                sub.memberId == memberId &&
                sub.gameId == gameId &&
                (excludeId == null || sub.id != excludeId) &&
                sub.startDate <= endDate &&
                sub.endDate >= startDate
            }
        }

        override suspend fun getOverlappingSubscriptions(
            memberId: Long,
            gameId: Long,
            startDate: Long,
            endDate: Long,
            excludeId: Long?
        ): List<SubscriptionEntity> {
            return subscriptions.filter { sub ->
                sub.memberId == memberId &&
                sub.gameId == gameId &&
                (excludeId == null || sub.id != excludeId) &&
                sub.startDate <= endDate &&
                sub.endDate >= startDate
            }
        }
    }

    private lateinit var memberDao: FakeMemberDao
    private lateinit var subscriptionDao: FakeSubscriptionDao
    private lateinit var repository: SubscriptionRepositoryImpl

    @Before
    fun setUp() {
        memberDao = FakeMemberDao()
        subscriptionDao = FakeSubscriptionDao()
        repository = SubscriptionRepositoryImpl(subscriptionDao, memberDao)

        runBlocking {
            memberDao.insert(MemberEntity(id = 1L, name = "أحمد", phone = "01000000000", gender = "ذكر", joinDate = 100L))
        }
    }

    @Test
    fun testAddFirstSubscription_succeeds() = runBlocking {
        val sub = SubscriptionEntity(
            memberId = 1L,
            gameId = 10L,
            planType = PlanType.MONTHLY,
            price = 500.0,
            startDate = 100L,
            endDate = 130L
        )

        val result = repository.addSubscription(sub)
        assertTrue(result is OperationResult.Success)
        assertEquals(1L, (result as OperationResult.Success).data)
    }

    @Test
    fun testAddOverlappingSubscription_returnsOverlapError() = runBlocking {
        val existing = SubscriptionEntity(
            memberId = 1L,
            gameId = 10L,
            planType = PlanType.MONTHLY,
            price = 500.0,
            startDate = 100L,
            endDate = 130L
        )
        subscriptionDao.insert(existing)

        val overlappingSub = SubscriptionEntity(
            memberId = 1L,
            gameId = 10L,
            planType = PlanType.MONTHLY,
            price = 500.0,
            startDate = 120L,
            endDate = 150L
        )

        val result = repository.addSubscription(overlappingSub)
        assertTrue(result is OperationResult.OverlapError)
        val overlapErr = result as OperationResult.OverlapError
        assertEquals(131L, overlapErr.suggestedStartDateEpochDay)
    }

    @Test
    fun testAddAllowedUpcomingRenewal_succeeds() = runBlocking {
        val existing = SubscriptionEntity(
            memberId = 1L,
            gameId = 10L,
            planType = PlanType.MONTHLY,
            price = 500.0,
            startDate = 100L,
            endDate = 130L
        )
        subscriptionDao.insert(existing)

        val renewalSub = SubscriptionEntity(
            memberId = 1L,
            gameId = 10L,
            planType = PlanType.MONTHLY,
            price = 500.0,
            startDate = 131L,
            endDate = 161L
        )

        val result = repository.addSubscription(renewalSub)
        assertTrue(result is OperationResult.Success)
    }

    @Test
    fun testAddSubscriptionForArchivedMember_returnsArchivedError() = runBlocking {
        memberDao.archive(1L)

        val sub = SubscriptionEntity(
            memberId = 1L,
            gameId = 10L,
            planType = PlanType.MONTHLY,
            price = 500.0,
            startDate = 200L,
            endDate = 230L
        )

        val result = repository.addSubscription(sub)
        assertTrue(result is OperationResult.ArchivedMemberError)
    }
}
