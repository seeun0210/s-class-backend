package com.sclass.supporters.lesson.usecase

import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonScheduleAlreadyExistsException
import com.sclass.domain.domains.lesson.exception.LessonScheduleConflictException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import com.sclass.supporters.lesson.dto.ScheduleLessonRequest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
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

class CreateLessonScheduleUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var aesTokenEncryptor: AesTokenEncryptor
    private lateinit var calendarClientProvider: ObjectProvider<CentralGoogleCalendarClient>
    private lateinit var calendarClient: CentralGoogleCalendarClient
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var useCase: CreateLessonScheduleUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-0000000001"
    private val fixedNow = LocalDateTime.of(2026, 4, 26, 12, 30)
    private val zoneId = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(fixedNow.atZone(zoneId).toInstant(), zoneId)

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        centralGoogleAccountAdaptor = mockk()
        userAdaptor = mockk()
        aesTokenEncryptor = mockk()
        calendarClientProvider = mockk()
        calendarClient = mockk()
        txTemplate = mockk()
        every { txTemplate.execute(any<TransactionCallback<Any?>>()) } answers {
            firstArg<TransactionCallback<Any?>>().doInTransaction(mockk())
        }
        useCase =
            CreateLessonScheduleUseCase(
                lessonAdaptor = lessonAdaptor,
                centralGoogleAccountAdaptor = centralGoogleAccountAdaptor,
                userAdaptor = userAdaptor,
                aesTokenEncryptor = aesTokenEncryptor,
                centralGoogleCalendarClientProvider = calendarClientProvider,
                txTemplate = txTemplate,
                clock = clock,
            )
    }

    @Test
    fun `일정이 없는 수업에 스케줄을 생성하면 중앙 Google Meet을 생성하고 lesson에 저장한다`() {
        val lesson = lesson()
        val account = centralGoogleAccount()
        val commandSlot = slot<GoogleCalendarEventCreateCommand>()
        val scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)

        every { lessonAdaptor.findById(1L) } returns lesson
        every {
            lessonAdaptor.existsScheduleConflict(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                scheduledAt = scheduledAt,
                requestedDurationMinutes = 60L,
                excludeLessonId = 1L,
            )
        } returns false
        every { calendarClientProvider.getIfAvailable() } returns calendarClient
        every { centralGoogleAccountAdaptor.findGoogle() } returns account
        every { aesTokenEncryptor.decrypt("encrypted-refresh-token") } returns "refresh-token"
        every { userAdaptor.findById(teacherUserId) } returns user(teacherUserId, "teacher@example.com")
        every { userAdaptor.findById(studentUserId) } returns user(studentUserId, "student@example.com")
        every {
            calendarClient.createMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                command = capture(commandSlot),
            )
        } returns GoogleCalendarEventResult("event-id", "https://meet.google.com/abc-defg-hij", "abc-defg-hij")

        val response =
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(name = "변경된 수업명", scheduledAt = scheduledAt),
            )

        assertAll(
            { assertEquals("변경된 수업명", lesson.name) },
            { assertEquals(scheduledAt, lesson.scheduledAt) },
            { assertEquals("event-id", lesson.googleMeet?.calendarEventId) },
            { assertEquals("https://meet.google.com/abc-defg-hij", lesson.googleMeet?.joinUrl) },
            { assertEquals("abc-defg-hij", lesson.googleMeet?.code) },
            { assertEquals("https://meet.google.com/abc-defg-hij", response.googleMeetJoinUrl) },
            { assertEquals("abc-defg-hij", response.googleMeetCode) },
            { assertEquals(fixedNow, account.lastUsedAt) },
            { assertEquals("변경된 수업명", commandSlot.captured.summary) },
            { assertEquals(scheduledAt.atZone(zoneId), commandSlot.captured.startAt) },
            { assertEquals(scheduledAt.plusMinutes(60).atZone(zoneId), commandSlot.captured.endAt) },
            { assertEquals(listOf("teacher@example.com", "student@example.com"), commandSlot.captured.attendeeEmails) },
        )
        verify(exactly = 2) { txTemplate.execute(any<TransactionCallback<Any?>>()) }
    }

    @Test
    fun `담당 선생님이 아니면 스케줄 생성 불가`() {
        every { lessonAdaptor.findById(1L) } returns lesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute(
                userId = "other-teacher-id-000000001",
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)),
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `학생이나 선생님의 기존 수업과 시간이 겹치면 스케줄 생성 불가`() {
        val lesson = lesson()
        val scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)

        every { lessonAdaptor.findById(1L) } returns lesson
        every {
            lessonAdaptor.existsScheduleConflict(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                scheduledAt = scheduledAt,
                requestedDurationMinutes = 60L,
                excludeLessonId = 1L,
            )
        } returns true

        assertThrows<LessonScheduleConflictException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = scheduledAt),
            )
        }

        assertNull(lesson.scheduledAt)
        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `Calendar event 생성 후 저장 직전 충돌이 감지되면 생성한 이벤트를 삭제한다`() {
        val lesson = lesson()
        val account = centralGoogleAccount()
        val scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)

        every { lessonAdaptor.findById(1L) } returns lesson
        every {
            lessonAdaptor.existsScheduleConflict(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                scheduledAt = scheduledAt,
                requestedDurationMinutes = 60L,
                excludeLessonId = 1L,
            )
        } returnsMany listOf(false, true)
        every { calendarClientProvider.getIfAvailable() } returns calendarClient
        every { centralGoogleAccountAdaptor.findGoogle() } returns account
        every { aesTokenEncryptor.decrypt("encrypted-refresh-token") } returns "refresh-token"
        every { userAdaptor.findById(teacherUserId) } returns user(teacherUserId, "teacher@example.com")
        every { userAdaptor.findById(studentUserId) } returns user(studentUserId, "student@example.com")
        every {
            calendarClient.createMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                command = any(),
            )
        } returns GoogleCalendarEventResult("event-id", "https://meet.google.com/abc-defg-hij", "abc-defg-hij")
        every {
            calendarClient.deleteMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                eventId = "event-id",
            )
        } just Runs

        assertThrows<LessonScheduleConflictException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = scheduledAt),
            )
        }

        assertAll(
            { assertNull(lesson.scheduledAt) },
            { assertNull(lesson.googleMeet) },
        )
        verify {
            calendarClient.deleteMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                eventId = "event-id",
            )
        }
    }

    @Test
    fun `이미 일정이 있으면 스케줄 생성 불가`() {
        every { lessonAdaptor.findById(1L) } returns lesson(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0))

        assertThrows<LessonScheduleAlreadyExistsException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = LocalDateTime.of(2026, 5, 2, 20, 0)),
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `예정 상태가 아닌 수업은 Calendar 호출 전에 스케줄 생성 불가`() {
        every { lessonAdaptor.findById(1L) } returns lesson(status = LessonStatus.IN_PROGRESS)

        assertThrows<LessonInvalidStatusTransitionException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)),
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `중앙 Google Calendar client가 없으면 예외`() {
        val lesson = lesson()
        val scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)
        every { lessonAdaptor.findById(1L) } returns lesson
        every {
            lessonAdaptor.existsScheduleConflict(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                scheduledAt = scheduledAt,
                requestedDurationMinutes = 60L,
                excludeLessonId = 1L,
            )
        } returns false
        every { calendarClientProvider.getIfAvailable() } returns null

        assertThrows<GoogleCalendarCentralDisabledException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = scheduledAt),
            )
        }

        assertNull(lesson.googleMeet)
    }

    private fun lesson(
        scheduledAt: LocalDateTime? = null,
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
    )

    private fun user(
        id: String,
        email: String,
    ) = User(
        id = id,
        email = email,
        name = "user",
        authProvider = AuthProvider.EMAIL,
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
