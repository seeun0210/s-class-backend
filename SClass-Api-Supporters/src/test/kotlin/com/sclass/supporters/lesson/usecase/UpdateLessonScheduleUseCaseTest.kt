package com.sclass.supporters.lesson.usecase

import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonGoogleMeet
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonScheduleConflictException
import com.sclass.domain.domains.lesson.exception.LessonScheduleNotFoundException
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.ObjectProvider
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class UpdateLessonScheduleUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var aesTokenEncryptor: AesTokenEncryptor
    private lateinit var calendarClientProvider: ObjectProvider<CentralGoogleCalendarClient>
    private lateinit var calendarClient: CentralGoogleCalendarClient
    private lateinit var useCase: UpdateLessonScheduleUseCase

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
        useCase =
            UpdateLessonScheduleUseCase(
                lessonAdaptor = lessonAdaptor,
                centralGoogleAccountAdaptor = centralGoogleAccountAdaptor,
                userAdaptor = userAdaptor,
                aesTokenEncryptor = aesTokenEncryptor,
                centralGoogleCalendarClientProvider = calendarClientProvider,
                clock = clock,
            )
    }

    @Test
    fun `기존 Google Meet이 있으면 중앙 Calendar event를 수정한다`() {
        val lesson =
            lesson(
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
                googleMeet = LessonGoogleMeet("old-event-id", "https://meet.google.com/old-code", "old-code"),
            )
        val account = centralGoogleAccount()
        val commandSlot = slot<GoogleCalendarEventCreateCommand>()
        val newScheduledAt = LocalDateTime.of(2026, 5, 2, 21, 0)

        every { lessonAdaptor.findById(1L) } returns lesson
        every {
            lessonAdaptor.existsScheduleConflict(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                scheduledAt = newScheduledAt,
                durationMinutes = 60L,
                excludeLessonId = 1L,
            )
        } returns false
        every { calendarClientProvider.getIfAvailable() } returns calendarClient
        every { centralGoogleAccountAdaptor.findGoogle() } returns account
        every { aesTokenEncryptor.decrypt("encrypted-refresh-token") } returns "refresh-token"
        every { userAdaptor.findById(teacherUserId) } returns user(teacherUserId, "teacher@example.com")
        every { userAdaptor.findById(studentUserId) } returns user(studentUserId, "student@example.com")
        every {
            calendarClient.updateMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                eventId = "old-event-id",
                command = capture(commandSlot),
            )
        } returns GoogleCalendarEventResult("old-event-id", "https://meet.google.com/new-code", "new-code")

        val response =
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(name = "수학 보강", scheduledAt = newScheduledAt),
            )

        assertAll(
            { assertEquals("수학 보강", lesson.name) },
            { assertEquals(newScheduledAt, lesson.scheduledAt) },
            { assertEquals("old-event-id", lesson.googleMeet?.calendarEventId) },
            { assertEquals("https://meet.google.com/new-code", lesson.googleMeet?.joinUrl) },
            { assertEquals("new-code", lesson.googleMeet?.code) },
            { assertEquals("https://meet.google.com/new-code", response.googleMeetJoinUrl) },
            { assertEquals(fixedNow, account.lastUsedAt) },
            { assertEquals("수학 보강", commandSlot.captured.summary) },
            { assertEquals(newScheduledAt.atZone(zoneId), commandSlot.captured.startAt) },
            { assertEquals(newScheduledAt.plusMinutes(60).atZone(zoneId), commandSlot.captured.endAt) },
        )
        verify(exactly = 0) { calendarClient.createMeetEventWithRefreshToken(any(), any()) }
    }

    @Test
    fun `기존 일정은 있지만 Google Meet이 없으면 create로 보정한다`() {
        val lesson = lesson(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0))
        val account = centralGoogleAccount()
        val newScheduledAt = LocalDateTime.of(2026, 5, 2, 21, 0)

        every { lessonAdaptor.findById(1L) } returns lesson
        every {
            lessonAdaptor.existsScheduleConflict(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                scheduledAt = newScheduledAt,
                durationMinutes = 60L,
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
                command = any(),
            )
        } returns GoogleCalendarEventResult("new-event-id", "https://meet.google.com/new-code", "new-code")

        useCase.execute(
            userId = teacherUserId,
            lessonId = 1L,
            request = ScheduleLessonRequest(scheduledAt = newScheduledAt),
        )

        assertAll(
            { assertEquals("new-event-id", lesson.googleMeet?.calendarEventId) },
            { assertEquals("https://meet.google.com/new-code", lesson.googleMeet?.joinUrl) },
            { assertEquals("new-code", lesson.googleMeet?.code) },
            { assertEquals(fixedNow, account.lastUsedAt) },
        )
        verify(exactly = 0) { calendarClient.updateMeetEventWithRefreshToken(any(), any(), any()) }
    }

    @Test
    fun `학생이나 선생님의 기존 수업과 시간이 겹치면 스케줄 수정 불가`() {
        val originalScheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)
        val newScheduledAt = LocalDateTime.of(2026, 5, 2, 21, 0)
        val lesson = lesson(scheduledAt = originalScheduledAt)

        every { lessonAdaptor.findById(1L) } returns lesson
        every {
            lessonAdaptor.existsScheduleConflict(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                scheduledAt = newScheduledAt,
                durationMinutes = 60L,
                excludeLessonId = 1L,
            )
        } returns true

        assertThrows<LessonScheduleConflictException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(name = "변경 불가", scheduledAt = newScheduledAt),
            )
        }

        assertAll(
            { assertEquals("수학 1회차", lesson.name) },
            { assertEquals(originalScheduledAt, lesson.scheduledAt) },
        )
        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `일정이 없는 수업은 수정할 수 없다`() {
        every { lessonAdaptor.findById(1L) } returns lesson(scheduledAt = null)

        assertThrows<LessonScheduleNotFoundException> {
            useCase.execute(
                userId = teacherUserId,
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = LocalDateTime.of(2026, 5, 2, 21, 0)),
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    @Test
    fun `담당 선생님이 아니면 스케줄 수정 불가`() {
        every { lessonAdaptor.findById(1L) } returns lesson(scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0))

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute(
                userId = "other-teacher-id-000000001",
                lessonId = 1L,
                request = ScheduleLessonRequest(scheduledAt = LocalDateTime.of(2026, 5, 2, 21, 0)),
            )
        }

        verify(exactly = 0) { calendarClientProvider.getIfAvailable() }
    }

    private fun lesson(
        scheduledAt: LocalDateTime?,
        googleMeet: LessonGoogleMeet? = null,
    ) = Lesson(
        id = 1L,
        lessonType = LessonType.COURSE,
        enrollmentId = 1L,
        studentUserId = studentUserId,
        assignedTeacherUserId = teacherUserId,
        lessonNumber = 1,
        name = "수학 1회차",
        scheduledAt = scheduledAt,
        status = LessonStatus.SCHEDULED,
        googleMeet = googleMeet,
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
