package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.LessonGoogleMeet
import com.sclass.domain.domains.lesson.exception.LessonScheduleNotFoundException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import com.sclass.supporters.lesson.dto.LessonResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class DeleteLessonScheduleUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val aesTokenEncryptor: AesTokenEncryptor,
    private val centralGoogleCalendarClientProvider: ObjectProvider<CentralGoogleCalendarClient>,
    private val txTemplate: TransactionTemplate,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val log = LoggerFactory.getLogger(DeleteLessonScheduleUseCase::class.java)

    fun execute(
        userId: String,
        lessonId: Long,
    ): LessonResponse {
        val deleted = deleteSchedule(userId, lessonId)
        deleted.calendarEvent?.let { deleteCalendarEvent(lessonId, it) }
        return deleted.response
    }

    private fun deleteSchedule(
        userId: String,
        lessonId: Long,
    ): DeletedSchedule =
        txTemplate.execute {
            val lesson = lessonAdaptor.findByIdForUpdate(lessonId)
            if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()
            if (lesson.scheduledAt == null) throw LessonScheduleNotFoundException()
            lesson.validateScheduleUpdatable()

            val eventId = lesson.googleMeet?.calendarEventId?.takeIf { it.isNotBlank() }
            val calendarEvent =
                eventId?.let {
                    val calendarClient =
                        centralGoogleCalendarClientProvider.getIfAvailable()
                            ?: throw GoogleCalendarCentralDisabledException()
                    val account = centralGoogleAccountAdaptor.findGoogle()
                    val refreshToken = aesTokenEncryptor.decrypt(account.encryptedRefreshToken)

                    CalendarEventDelete(
                        eventId = it,
                        calendarClient = calendarClient,
                        refreshToken = refreshToken,
                        scheduledAt = lesson.scheduledAt!!,
                        googleMeet = lesson.googleMeet!!,
                    )
                }

            lesson.deleteSchedule()
            lessonAdaptor.save(lesson)

            DeletedSchedule(
                response = LessonResponse.from(lesson),
                calendarEvent = calendarEvent,
            )
        }!!

    private fun deleteCalendarEvent(
        lessonId: Long,
        calendarEvent: CalendarEventDelete,
    ) {
        try {
            calendarEvent.calendarClient.deleteMeetEventWithRefreshToken(
                refreshToken = calendarEvent.refreshToken,
                eventId = calendarEvent.eventId,
                sendUpdates = true,
            )
            markCalendarUsed()
        } catch (e: RuntimeException) {
            restoreSchedule(lessonId, calendarEvent)
            throw e
        }
    }

    private fun markCalendarUsed() {
        runCatching {
            txTemplate.execute {
                val centralAccount = centralGoogleAccountAdaptor.findGoogle()
                centralAccount.markUsed(LocalDateTime.now(clock))
                centralGoogleAccountAdaptor.save(centralAccount)
            }
        }.onFailure {
            log.warn("Failed to mark central Google account as used after lesson schedule delete", it)
        }
    }

    private fun restoreSchedule(
        lessonId: Long,
        calendarEvent: CalendarEventDelete,
    ) {
        runCatching {
            txTemplate.execute {
                val lesson = lessonAdaptor.findByIdForUpdate(lessonId)
                if (lesson.scheduledAt == null && lesson.googleMeet == null) {
                    lesson.updateSchedule(null, calendarEvent.scheduledAt)
                    lesson.attachGoogleMeet(
                        eventId = calendarEvent.googleMeet.calendarEventId,
                        meetJoinUrl = calendarEvent.googleMeet.joinUrl,
                        meetCode = calendarEvent.googleMeet.code,
                    )
                    lessonAdaptor.save(lesson)
                }
            }
        }.onFailure {
            log.warn("Failed to restore lesson schedule after Google Calendar event delete failed: lessonId={}", lessonId, it)
        }
    }

    private data class DeletedSchedule(
        val response: LessonResponse,
        val calendarEvent: CalendarEventDelete? = null,
    )

    private data class CalendarEventDelete(
        val eventId: String,
        val calendarClient: CentralGoogleCalendarClient,
        val refreshToken: String,
        val scheduledAt: LocalDateTime,
        val googleMeet: LessonGoogleMeet,
    )
}
