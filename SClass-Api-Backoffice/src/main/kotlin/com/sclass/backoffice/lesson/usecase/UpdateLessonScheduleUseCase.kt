package com.sclass.backoffice.lesson.usecase

import com.sclass.backoffice.lesson.dto.LessonResponse
import com.sclass.backoffice.lesson.dto.ScheduleLessonRequest
import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.exception.LessonScheduleConflictException
import com.sclass.domain.domains.lesson.exception.LessonScheduleNotFoundException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.transaction.support.TransactionTemplate
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
    private val txTemplate: TransactionTemplate,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val log = LoggerFactory.getLogger(UpdateLessonScheduleUseCase::class.java)

    fun execute(
        lessonId: Long,
        request: ScheduleLessonRequest,
    ): LessonResponse {
        val prepared = prepareSchedule(lessonId, request)
        val result =
            if (prepared.eventId.isNullOrBlank()) {
                prepared.calendarClient.createMeetEventWithRefreshToken(
                    refreshToken = prepared.refreshToken,
                    command = prepared.command,
                )
            } else {
                prepared.calendarClient.updateMeetEventWithRefreshToken(
                    refreshToken = prepared.refreshToken,
                    eventId = prepared.eventId,
                    command = prepared.command,
                )
            }

        return try {
            saveSchedule(lessonId, request, prepared.scheduledAt, result)
        } catch (e: RuntimeException) {
            rollbackCalendarEvent(prepared, result)
            throw e
        }
    }

    private fun prepareSchedule(
        lessonId: Long,
        request: ScheduleLessonRequest,
    ): PreparedUpdateSchedule =
        txTemplate.execute {
            val scheduledAt = requireNotNull(request.scheduledAt)
            val lesson = lessonAdaptor.findById(lessonId)
            val currentScheduledAt = lesson.scheduledAt ?: throw LessonScheduleNotFoundException()
            lesson.validateScheduleUpdatable()

            validateNoScheduleConflict(lesson, scheduledAt)

            val calendarClient =
                centralGoogleCalendarClientProvider.getIfAvailable()
                    ?: throw GoogleCalendarCentralDisabledException()
            val centralAccount = centralGoogleAccountAdaptor.findGoogle()
            val refreshToken = aesTokenEncryptor.decrypt(centralAccount.encryptedRefreshToken)

            PreparedUpdateSchedule(
                scheduledAt = scheduledAt,
                command = lesson.toGoogleCalendarCommand(request.name, scheduledAt),
                previousCommand = lesson.toGoogleCalendarCommand(lesson.name, currentScheduledAt),
                calendarClient = calendarClient,
                refreshToken = refreshToken,
                eventId = lesson.googleMeet?.calendarEventId,
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
            val lesson = lessonAdaptor.findByIdForUpdate(lessonId)
            if (lesson.scheduledAt == null) throw LessonScheduleNotFoundException()
            lesson.validateScheduleUpdatable()
            validateNoScheduleConflict(lesson, scheduledAt)

            lesson.updateSchedule(request.name, scheduledAt)
            lesson.attachGoogleMeet(
                eventId = result.eventId,
                meetJoinUrl = result.meetJoinUrl,
                meetCode = result.meetCode,
            )
            lessonAdaptor.save(lesson)

            val centralAccount = centralGoogleAccountAdaptor.findGoogle()
            centralAccount.markUsed(LocalDateTime.now(clock))
            centralGoogleAccountAdaptor.save(centralAccount)
            LessonResponse.from(lesson)
        }!!

    private fun rollbackCalendarEvent(
        prepared: PreparedUpdateSchedule,
        result: GoogleCalendarEventResult,
    ) {
        if (prepared.eventId.isNullOrBlank()) {
            deleteCreatedCalendarEvent(prepared, result.eventId)
        } else {
            restoreUpdatedCalendarEvent(prepared)
        }
    }

    private fun deleteCreatedCalendarEvent(
        prepared: PreparedUpdateSchedule,
        eventId: String,
    ) {
        runCatching {
            prepared.calendarClient.deleteMeetEventWithRefreshToken(
                refreshToken = prepared.refreshToken,
                eventId = eventId,
            )
        }.onFailure {
            log.warn("Failed to delete Google Calendar event after lesson schedule save failed: eventId={}", eventId, it)
        }
    }

    private fun restoreUpdatedCalendarEvent(prepared: PreparedUpdateSchedule) {
        runCatching {
            prepared.calendarClient.updateMeetEventWithRefreshToken(
                refreshToken = prepared.refreshToken,
                eventId = prepared.eventId!!,
                command = prepared.previousCommand,
            )
        }.onFailure {
            log.warn(
                "Failed to restore Google Calendar event after lesson schedule save failed: eventId={}",
                prepared.eventId,
                it,
            )
        }
    }

    private fun Lesson.toGoogleCalendarCommand(
        name: String?,
        scheduledAt: LocalDateTime,
    ): GoogleCalendarEventCreateCommand {
        val teacher = userAdaptor.findById(effectiveTeacherUserId)
        val student = userAdaptor.findById(studentUserId)

        return GoogleCalendarEventCreateCommand(
            summary = name ?: this.name,
            startAt = scheduledAt.atZone(SEOUL_ZONE_ID),
            endAt =
                scheduledAt.plusMinutes(Lesson.DEFAULT_DURATION_MINUTES).atZone(SEOUL_ZONE_ID),
            attendeeEmails =
                listOf(
                    teacher.email,
                    student.email,
                ).distinct(),
        )
    }

    private data class PreparedUpdateSchedule(
        val scheduledAt: LocalDateTime,
        val command: GoogleCalendarEventCreateCommand,
        val previousCommand: GoogleCalendarEventCreateCommand,
        val calendarClient: CentralGoogleCalendarClient,
        val refreshToken: String,
        val eventId: String?,
    )

    private companion object {
        val SEOUL_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
