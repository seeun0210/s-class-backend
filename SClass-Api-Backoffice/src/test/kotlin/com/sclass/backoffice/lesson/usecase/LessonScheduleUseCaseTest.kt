package com.sclass.backoffice.lesson.usecase

import com.sclass.backoffice.lesson.dto.ScheduleLessonRequest
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonGoogleMeet
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
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
import org.springframework.beans.factory.ObjectProvider
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class LessonScheduleUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var aesTokenEncryptor: AesTokenEncryptor
    private lateinit var calendarClientProvider: ObjectProvider<CentralGoogleCalendarClient>
    private lateinit var calendarClient: CentralGoogleCalendarClient
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var createUseCase: CreateLessonScheduleUseCase
    private lateinit var updateUseCase: UpdateLessonScheduleUseCase
    private lateinit var deleteUseCase: DeleteLessonScheduleUseCase

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
        createUseCase =
            CreateLessonScheduleUseCase(
                lessonAdaptor = lessonAdaptor,
                centralGoogleAccountAdaptor = centralGoogleAccountAdaptor,
                userAdaptor = userAdaptor,
                aesTokenEncryptor = aesTokenEncryptor,
                centralGoogleCalendarClientProvider = calendarClientProvider,
                txTemplate = txTemplate,
                clock = clock,
            )
        updateUseCase =
            UpdateLessonScheduleUseCase(
                lessonAdaptor = lessonAdaptor,
                centralGoogleAccountAdaptor = centralGoogleAccountAdaptor,
                userAdaptor = userAdaptor,
                aesTokenEncryptor = aesTokenEncryptor,
                centralGoogleCalendarClientProvider = calendarClientProvider,
                txTemplate = txTemplate,
                clock = clock,
            )
        deleteUseCase =
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
    fun `관리자가 수업 스케줄을 생성하면 중앙 Google Meet을 생성하고 lesson에 저장한다`() {
        val lesson = lesson()
        val account = centralGoogleAccount()
        val commandSlot = slot<GoogleCalendarEventCreateCommand>()
        val scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0)

        mockSchedulePreparation(lesson, account, scheduledAt)
        every {
            calendarClient.createMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                command = capture(commandSlot),
            )
        } returns GoogleCalendarEventResult("event-id", "https://meet.google.com/abc-defg-hij", "abc-defg-hij")

        val response =
            createUseCase.execute(
                lessonId = 1L,
                request = ScheduleLessonRequest(name = "관리자 예약", scheduledAt = scheduledAt),
            )

        assertAll(
            { assertEquals("관리자 예약", lesson.name) },
            { assertEquals(scheduledAt, lesson.scheduledAt) },
            { assertEquals("event-id", lesson.googleMeet?.calendarEventId) },
            { assertEquals("https://meet.google.com/abc-defg-hij", response.googleMeetJoinUrl) },
            { assertEquals("abc-defg-hij", response.googleMeetCode) },
            { assertEquals(fixedNow, account.lastUsedAt) },
            { assertEquals("관리자 예약", commandSlot.captured.summary) },
            { assertEquals(listOf("teacher@example.com", "student@example.com"), commandSlot.captured.attendeeEmails) },
        )
    }

    @Test
    fun `관리자가 기존 Google Meet 수업 스케줄을 수정하면 중앙 Calendar event를 수정한다`() {
        val lesson =
            lesson(
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
                googleMeet = LessonGoogleMeet("event-id", "https://meet.google.com/old-code", "old-code"),
            )
        val account = centralGoogleAccount()
        val commandSlot = slot<GoogleCalendarEventCreateCommand>()
        val newScheduledAt = LocalDateTime.of(2026, 5, 2, 21, 0)

        mockSchedulePreparation(lesson, account, newScheduledAt)
        every {
            calendarClient.updateMeetEventWithRefreshToken(
                refreshToken = "refresh-token",
                eventId = "event-id",
                command = capture(commandSlot),
            )
        } returns GoogleCalendarEventResult("event-id", "https://meet.google.com/new-code", "new-code")

        val response =
            updateUseCase.execute(
                lessonId = 1L,
                request = ScheduleLessonRequest(name = "관리자 변경", scheduledAt = newScheduledAt),
            )

        assertAll(
            { assertEquals("관리자 변경", lesson.name) },
            { assertEquals(newScheduledAt, lesson.scheduledAt) },
            { assertEquals("event-id", lesson.googleMeet?.calendarEventId) },
            { assertEquals("https://meet.google.com/new-code", response.googleMeetJoinUrl) },
            { assertEquals("new-code", response.googleMeetCode) },
            { assertEquals(fixedNow, account.lastUsedAt) },
            { assertEquals(newScheduledAt.atZone(zoneId), commandSlot.captured.startAt) },
        )
    }

    @Test
    fun `관리자가 수업 스케줄을 삭제하면 Calendar event를 취소하고 lesson 일정을 제거한다`() {
        val lesson =
            lesson(
                scheduledAt = LocalDateTime.of(2026, 5, 1, 20, 0),
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

        val response = deleteUseCase.execute(1L)

        assertAll(
            { assertNull(lesson.scheduledAt) },
            { assertNull(lesson.googleMeet) },
            { assertNull(response.scheduledAt) },
            { assertNull(response.googleMeetJoinUrl) },
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
    }

    private fun mockSchedulePreparation(
        lesson: Lesson,
        account: CentralGoogleAccount,
        scheduledAt: LocalDateTime,
    ) {
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
        every { lessonAdaptor.save(lesson) } returns lesson
        every { centralGoogleAccountAdaptor.save(account) } returns account
    }

    private fun lesson(
        scheduledAt: LocalDateTime? = null,
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
