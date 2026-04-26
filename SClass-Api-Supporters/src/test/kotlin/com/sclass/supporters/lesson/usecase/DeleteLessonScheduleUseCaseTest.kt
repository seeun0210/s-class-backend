package com.sclass.supporters.lesson.usecase

import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonGoogleMeet
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonScheduleNotFoundException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.ObjectProvider
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class DeleteLessonScheduleUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor
    private lateinit var aesTokenEncryptor: AesTokenEncryptor
    private lateinit var calendarClientProvider: ObjectProvider<CentralGoogleCalendarClient>
    private lateinit var calendarClient: CentralGoogleCalendarClient
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var useCase: DeleteLessonScheduleUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-0000000001"
    private val fixedNow = LocalDateTime.of(2026, 4, 26, 12, 30)
    private val zoneId = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(fixedNow.atZone(zoneId).toInstant(), zoneId)

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        centralGoogleAccountAdaptor = mockk()
        aesTokenEncryptor = mockk()
        calendarClientProvider = mockk()
        calendarClient = mockk()
        txTemplate = mockk()
        every { txTemplate.execute(any<TransactionCallback<Any?>>()) } answers {
            firstArg<TransactionCallback<Any?>>().doInTransaction(mockk())
        }
        useCase =
            DeleteLessonScheduleUseCase(
                lessonAdaptor = lessonAdaptor,
                centralGoogleAccountAdaptor = centralGoogleAccountAdaptor,
                aesTokenEncryptor = aesTokenEncryptor,
                centralGoogleCalendarClientProvider = calendarClientProvider,
                txTemplate = txTemplate,
                clock = clock,
            )
    }

    @Test
    fun `Google Meet 일정이 있는 수업 스케줄을 삭제하면 Calendar event를 취소하고 lesson 일정을 제거한다`() {
        val scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)
        val lesson =
            lesson(
                scheduledAt = scheduledAt,
                googleMeet = LessonGoogleMeet("event-id", "https://meet.google.com/abc-defg-hij", "abc-defg-hij"),
            )
        val account = centralGoogleAccount()

        every { lessonAdaptor.findByIdForUpdate(1L) } returns lesson
        every { calendarClientProvider.getIfAvailable() } returns calendarClient
        every { centralGoogleAccountAdaptor.findGoogle() } returns account
        every { aesTokenEncryptor.decrypt("encrypted-refresh-token") } returns "refresh-token"
        every { lessonAdaptor.save(lesson) } returns lesson
        every { centralGoogleAccountAdaptor.save(account) } returns account
        every {
            calendarClient.deleteMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                eventId = "event-id",
                sendUpdates = true,
            )
        } just Runs

        val response =
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
            )

        assertAll(
            { assertNull(lesson.scheduledAt) },
            { assertNull(lesson.googleMeet) },
            { assertNull(response.scheduledAt) },
            { assertNull(response.googleMeetJoinUrl) },
            { assertNull(response.googleMeetCode) },
            { assertEquals(fixedNow, account.lastUsedAt) },
        )
        verify {
            calendarClient.deleteMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                eventId = "event-id",
                sendUpdates = true,
            )
        }
        verify(exactly = 1) { lessonAdaptor.save(lesson) }
        verify(exactly = 1) { centralGoogleAccountAdaptor.save(account) }
        verify(exactly = 1) { txTemplate.execute(any<TransactionCallback<Any?>>()) }
    }

    @Test
    fun `기존 일정은 있지만 Google Meet이 없으면 Calendar 호출 없이 lesson 일정만 제거한다`() {
        val lesson = lesson(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0))

        every { lessonAdaptor.findByIdForUpdate(1L) } returns lesson
        every { lessonAdaptor.save(lesson) } returns lesson

        val response =
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
            )

        assertAll(
            { assertNull(lesson.scheduledAt) },
            { assertNull(lesson.googleMeet) },
            { assertNull(response.scheduledAt) },
        )
        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
        verify(exactly = 1) { lessonAdaptor.save(lesson) }
    }

    @Test
    fun `일정이 없는 수업은 스케줄 삭제 불가`() {
        every { lessonAdaptor.findByIdForUpdate(1L) } returns lesson(scheduledAt = null)

        assertThrows<LessonScheduleNotFoundException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `예정 상태가 아닌 수업은 Calendar 호출 전에 스케줄 삭제 불가`() {
        every {
            lessonAdaptor.findByIdForUpdate(1L)
        } returns lesson(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0), status = LessonStatus.IN_PROGRESS)

        assertThrows<LessonInvalidStatusTransitionException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `담당 선생님이 아니면 스케줄 삭제 불가`() {
        every { lessonAdaptor.findByIdForUpdate(1L) } returns lesson(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0))

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute(
                userId = "other-teacher-id-000000001",
                lessonId = 1L,
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `Google Meet 일정이 있는데 중앙 Calendar client가 없으면 예외`() {
        val lesson =
            lesson(
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
                googleMeet = LessonGoogleMeet("event-id", "https://meet.google.com/abc-defg-hij", "abc-defg-hij"),
            )

        every { lessonAdaptor.findByIdForUpdate(1L) } returns lesson
        every { calendarClientProvider.getIfAvailable() } returns null

        assertThrows<GoogleCalendarCentralDisabledException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
            )
        }

        assertEquals(LocalDateTime.of(2026, 5, 1, 20, 0), lesson.scheduledAt)
    }

    private fun lesson(
        scheduledAt: LocalDateTime?,
        googleMeet: LessonGoogleMeet? = null,
        status: LessonStatus = LessonStatus.SCHEDULED,
    ) = Lesson(
        id = 1L,
        lessonType = LessonType.COURSE,
        enrollmentId = 1L,
        studentUserId = studentUserId,
        assignedTeacherUserId = teacherUserId,
        lessonNumber = 1,
        name = "수학 1회차",
        scheduledAt = scheduledAt,
        status = status,
        googleMeet = googleMeet,
    )

    private fun centralGoogleAccount() =
        CentralGoogleAccount(
            googleEmail = "central@example.com",
            encryptedRefreshToken = "encrypted-refresh-token",
            scope = "openid https://www.googleapis.com/auth/calendar.events",
            connectedByAdminUserId = "admin-user-id-000000000001",
            connectedAt = fixedNow.minusDays(1),
        )
}
