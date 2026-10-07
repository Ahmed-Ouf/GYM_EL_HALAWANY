package com.example.simplecalc

import com.example.simplecalc.data.local.dao.AttendanceDao
import com.example.simplecalc.data.local.dao.AttendanceWithGames
import com.example.simplecalc.data.local.dao.MemberDao
import com.example.simplecalc.data.local.entity.AttendanceEntity
import com.example.simplecalc.data.local.entity.AttendanceGameCrossRef
import com.example.simplecalc.data.local.entity.MemberEntity
import com.example.simplecalc.data.repository.AttendanceRepositoryImpl
import com.example.simplecalc.data.repository.OperationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AttendanceLogicTest {

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

    private class FakeAttendanceDao : AttendanceDao {
        val attendances = mutableListOf<AttendanceEntity>()
        val crossRefs = mutableListOf<AttendanceGameCrossRef>()
        var nextId = 1L

        override suspend fun insert(attendance: AttendanceEntity): Long {
            val entity = attendance.copy(id = nextId++)
            attendances.add(entity)
            return entity.id
        }

        override suspend fun insertCrossRef(crossRef: AttendanceGameCrossRef): Long {
            crossRefs.add(crossRef)
            return 1L
        }

        override suspend fun insertCrossRefs(crossRefs: List<AttendanceGameCrossRef>): List<Long> {
            this.crossRefs.addAll(crossRefs)
            return crossRefs.map { 1L }
        }

        override suspend fun updateCheckOut(id: Long, checkOut: Long): Int {
            val index = attendances.indexOfFirst { it.id == id }
            if (index != -1) {
                attendances[index] = attendances[index].copy(checkOut = checkOut)
                return 1
            }
            return 0
        }

        override suspend fun getOpenForMember(memberId: Long): AttendanceEntity? {
            return attendances.find { it.memberId == memberId && it.checkOut == null }
        }

        override fun getOpenForMemberFlow(memberId: Long): Flow<AttendanceEntity?> =
            flowOf(attendances.find { it.memberId == memberId && it.checkOut == null })

        override fun getTodayAttendanceFlow(todayStartMillis: Long): Flow<List<AttendanceEntity>> =
            flowOf(attendances.filter { it.checkIn >= todayStartMillis })

        override fun getAttendanceWithGames(id: Long) = flowOf(null)
        override fun getTodayAttendanceWithGamesFlow(todayStartMillis: Long) = flowOf(emptyList<AttendanceWithGames>())
        override fun getAttendanceForDayWithGames(startOfDayMillis: Long, endOfDayMillis: Long) = flowOf(emptyList<AttendanceWithGames>())
    }

    private lateinit var memberDao: FakeMemberDao
    private lateinit var attendanceDao: FakeAttendanceDao
    private lateinit var repository: AttendanceRepositoryImpl

    @Before
    fun setUp() {
        memberDao = FakeMemberDao()
        attendanceDao = FakeAttendanceDao()
        repository = AttendanceRepositoryImpl(attendanceDao, memberDao)

        runBlocking {
            memberDao.insert(MemberEntity(id = 1L, name = "سارة", phone = "01100000000", gender = "أنثى", joinDate = 100L))
        }
    }

    @Test
    fun testCheckInFirstTime_succeeds() = runBlocking {
        val attendance = AttendanceEntity(memberId = 1L, checkIn = 10000L)
        val result = repository.checkIn(attendance, listOf(10L, 20L))

        assertTrue(result is OperationResult.Success)
        val id = (result as OperationResult.Success).data
        assertEquals(1L, id)

        val open = repository.getOpenAttendance(1L)
        assertNotNull(open)
        assertEquals(10000L, open?.checkIn)
    }

    @Test
    fun testCheckInWhenAlreadyInside_fails() = runBlocking {
        val attendance1 = AttendanceEntity(memberId = 1L, checkIn = 10000L)
        repository.checkIn(attendance1, listOf(10L))

        // Second checkIn attempt without checking out first
        val attendance2 = AttendanceEntity(memberId = 1L, checkIn = 10500L)
        val result = repository.checkIn(attendance2, listOf(10L))

        assertTrue(result is OperationResult.Error)
        val err = result as OperationResult.Error
        assertEquals("العضو متواجد بالداخل حالياً.", err.message)
    }

    @Test
    fun testCheckOut_closesOpenSession() = runBlocking {
        val attendance = AttendanceEntity(memberId = 1L, checkIn = 10000L)
        val result = repository.checkIn(attendance, listOf(10L))
        val attendanceId = (result as OperationResult.Success).data

        // Perform CheckOut
        repository.checkOut(attendanceId, checkOutTime = 15000L)

        val open = repository.getOpenAttendance(1L)
        assertEquals(null, open)
    }

    @Test
    fun testCheckInForArchivedMember_returnsArchivedError() = runBlocking {
        memberDao.archive(1L)

        val attendance = AttendanceEntity(memberId = 1L, checkIn = 20000L)
        val result = repository.checkIn(attendance, listOf(10L))

        assertTrue(result is OperationResult.ArchivedMemberError)
    }
}
