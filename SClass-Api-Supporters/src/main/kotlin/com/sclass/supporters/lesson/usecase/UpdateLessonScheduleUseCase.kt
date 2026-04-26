package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.exception.LessonScheduleConflictException
import com.sclass.domain.domains.lesson.exception.LessonScheduleNotFoundException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.supporters.lesson.dto.LessonResponse
import com.sclass.supporters.lesson.dto.ScheduleLessonRequest
import org.springframework.beans.factory.ObjectProvider
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@UseCase
class UpdateLessonScheduleUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val userAdaptor: UserAdaptor,
    private val aesTokenEncryptor: AesTokenEncryptor,
    private val centralGoogleCalendarClientProvider: ObjectProvider<CentralGoogleCalendarClient>,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @Transactional
    fun execute(
        userId: String,
        lessonId: Long,
        request: ScheduleLessonRequest,
    ): LessonResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (!lesson.isTeacher(userId)) {
            throw LessonUnauthorizedAccessException()
        }

        if (lesson.scheduledAt == null) throw LessonScheduleNotFoundException()

        val scheduledAt = requireNotNull(request.scheduledAt)
        validateNoScheduleConflict(lesson, scheduledAt)

        lesson.updateSchedule(request.name, scheduledAt)
        syncGoogleMeet(lesson)

        return LessonResponse.from(lesson)
    }

    private fun validateNoScheduleConflict(
        lesson: Lesson,
        scheduledAt: LocalDateTime,
    ) {
        if (
            lessonAdaptor.existsScheduleConflict(
                studentUserId = lesson.studentUserId,
                teacherUserId = lesson.effectiveTeacherUserId,
                scheduledAt = scheduledAt,
                durationMinutes = DEFAULT_LESSON_DURATION_MINUTES,
                excludeLessonId = lesson.id,
            )
        ) {
            throw LessonScheduleConflictException()
        }
    }

    private fun syncGoogleMeet(lesson: Lesson) {
        val calendarClient =
            centralGoogleCalendarClientProvider.getIfAvailable()
                ?: throw GoogleCalendarCentralDisabledException()

        val centralAccount = centralGoogleAccountAdaptor.findGoogle()
        val refreshToken =
            aesTokenEncryptor.decrypt(centralAccount.encryptedRefreshToken)
        val command = lesson.toGoogleCalendarCommand()
        val eventId = lesson.googleMeet?.calendarEventId

        val result =
            if (eventId.isNullOrBlank()) {
                calendarClient.createMeetEventWithRefreshToken(refreshToken, command)
            } else {
                calendarClient.updateMeetEventWithRefreshToken(
                    refreshToken = refreshToken,
                    eventId = eventId,
                    command = command,
                )
            }

        lesson.attachGoogleMeet(
            eventId = result.eventId,
            meetJoinUrl = result.meetJoinUrl,
            meetCode = result.meetCode,
        )
        centralAccount.markUsed(LocalDateTime.now(clock))
    }

    private fun Lesson.toGoogleCalendarCommand(): GoogleCalendarEventCreateCommand {
        val scheduledAt = requireNotNull(scheduledAt)
        val teacher = userAdaptor.findById(effectiveTeacherUserId)
        val student = userAdaptor.findById(studentUserId)

        return GoogleCalendarEventCreateCommand(
            summary = name,
            startAt = scheduledAt.atZone(SEOUL_ZONE_ID),
            endAt =
                scheduledAt.plusMinutes(DEFAULT_LESSON_DURATION_MINUTES).atZone(SEOUL_ZONE_ID),
            attendeeEmails =
                listOf(
                    teacher.email,
                    student.email,
                ).distinct(),
        )
    }

    private companion object {
        val SEOUL_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        const val DEFAULT_LESSON_DURATION_MINUTES = 60L
    }
}
