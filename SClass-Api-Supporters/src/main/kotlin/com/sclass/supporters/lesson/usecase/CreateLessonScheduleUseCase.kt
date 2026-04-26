package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.exception.LessonScheduleAlreadyExistsException
import com.sclass.domain.domains.lesson.exception.LessonScheduleConflictException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import com.sclass.supporters.lesson.dto.LessonResponse
import com.sclass.supporters.lesson.dto.ScheduleLessonRequest
import org.springframework.beans.factory.ObjectProvider
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@UseCase
class CreateLessonScheduleUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val userAdaptor: UserAdaptor,
    private val aesTokenEncryptor: AesTokenEncryptor,
    private val centralGoogleCalendarClientProvider: ObjectProvider<CentralGoogleCalendarClient>,
    private val txTemplate: TransactionTemplate,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun execute(
        userId: String,
        lessonId: Long,
        request: ScheduleLessonRequest,
    ): LessonResponse {
        val prepared = prepareSchedule(userId, lessonId, request)
        val result =
            prepared.calendarClient.createMeetEventWithRefreshToken(
                refreshToken = prepared.refreshToken,
                command = prepared.command,
            )

        return saveSchedule(lessonId, request, prepared.scheduledAt, result)
    }

    private fun prepareSchedule(
        userId: String,
        lessonId: Long,
        request: ScheduleLessonRequest,
    ): PreparedCreateSchedule =
        txTemplate.execute {
            val scheduledAt = requireNotNull(request.scheduledAt)
            val lesson = lessonAdaptor.findById(lessonId)
            if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()
            if (lesson.scheduledAt != null) throw LessonScheduleAlreadyExistsException()

            validateNoScheduleConflict(lesson, scheduledAt)

            val calendarClient =
                centralGoogleCalendarClientProvider.getIfAvailable()
                    ?: throw GoogleCalendarCentralDisabledException()
            val centralAccount = centralGoogleAccountAdaptor.findGoogle()
            val refreshToken = aesTokenEncryptor.decrypt(centralAccount.encryptedRefreshToken)

            PreparedCreateSchedule(
                scheduledAt = scheduledAt,
                command = lesson.toGoogleCalendarCommand(request.name, scheduledAt),
                calendarClient = calendarClient,
                refreshToken = refreshToken,
            )
        }!!

    private fun validateNoScheduleConflict(
        lesson: Lesson,
        scheduledAt: LocalDateTime,
    ) {
        if (
            lessonAdaptor.existsScheduleConflict(
                studentUserId = lesson.studentUserId,
                teacherUserId = lesson.effectiveTeacherUserId,
                scheduledAt = scheduledAt,
                requestedDurationMinutes = Lesson.DEFAULT_DURATION_MINUTES,
                excludeLessonId = lesson.id,
            )
        ) {
            throw LessonScheduleConflictException()
        }
    }

    private fun saveSchedule(
        lessonId: Long,
        request: ScheduleLessonRequest,
        scheduledAt: LocalDateTime,
        result: GoogleCalendarEventResult,
    ): LessonResponse =
        txTemplate.execute {
            val lesson = lessonAdaptor.findById(lessonId)
            lesson.updateSchedule(request.name, scheduledAt)
            lesson.attachGoogleMeet(
                eventId = result.eventId,
                meetJoinUrl = result.meetJoinUrl,
                meetCode = result.meetCode,
            )

            val centralAccount = centralGoogleAccountAdaptor.findGoogle()
            centralAccount.markUsed(LocalDateTime.now(clock))
            LessonResponse.from(lesson)
        }!!

    private fun Lesson.toGoogleCalendarCommand(
        name: String?,
        scheduledAt: LocalDateTime,
    ): GoogleCalendarEventCreateCommand {
        val teacher = userAdaptor.findById(effectiveTeacherUserId)
        val student = userAdaptor.findById(studentUserId)

        return GoogleCalendarEventCreateCommand(
            summary = name ?: this.name,
            startAt = scheduledAt.atZone(SEOUL_ZONE_ID),
            endAt = scheduledAt.plusMinutes(Lesson.DEFAULT_DURATION_MINUTES).atZone(SEOUL_ZONE_ID),
            attendeeEmails = listOf(teacher.email, student.email).distinct(),
        )
    }

    private data class PreparedCreateSchedule(
        val scheduledAt: LocalDateTime,
        val command: GoogleCalendarEventCreateCommand,
        val calendarClient: CentralGoogleCalendarClient,
        val refreshToken: String,
    )

    private companion object {
        val SEOUL_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
