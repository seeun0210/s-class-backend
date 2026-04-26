package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonScheduleNotFoundException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
import com.sclass.supporters.lesson.dto.LessonResponse
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
    fun execute(
        userId: String,
        lessonId: Long,
    ): LessonResponse {
        val prepared = prepareScheduleDelete(userId, lessonId)

        prepared.calendarEvent?.let {
            it.calendarClient.deleteMeetEventWithRefreshToken(
                refreshToken = it.refreshToken,
                eventId = it.eventId,
                sendUpdates = true,
            )
        }

        return deleteSchedule(userId, lessonId, prepared.calendarEvent != null)
    }

    private fun prepareScheduleDelete(
        userId: String,
        lessonId: Long,
    ): PreparedDeleteSchedule =
        txTemplate.execute {
            val lesson = lessonAdaptor.findById(lessonId)
            if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()
            if (lesson.scheduledAt == null) throw LessonScheduleNotFoundException()
            lesson.validateScheduleUpdatable()

            val eventId = lesson.googleMeet?.calendarEventId?.takeIf { it.isNotBlank() }
            if (eventId == null) {
                PreparedDeleteSchedule()
            } else {
                val calendarClient =
                    centralGoogleCalendarClientProvider.getIfAvailable()
                        ?: throw GoogleCalendarCentralDisabledException()
                val centralAccount = centralGoogleAccountAdaptor.findGoogle()
                val refreshToken = aesTokenEncryptor.decrypt(centralAccount.encryptedRefreshToken)

                PreparedDeleteSchedule(CalendarEventDelete(eventId, calendarClient, refreshToken))
            }
        }!!

    private fun deleteSchedule(
        userId: String,
        lessonId: Long,
        calendarEventDeleted: Boolean,
    ): LessonResponse =
        txTemplate.execute {
            val lesson = lessonAdaptor.findById(lessonId)
            if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()
            if (lesson.scheduledAt == null) throw LessonScheduleNotFoundException()
            lesson.validateScheduleUpdatable()

            lesson.deleteSchedule()

            if (calendarEventDeleted) {
                val centralAccount = centralGoogleAccountAdaptor.findGoogle()
                centralAccount.markUsed(LocalDateTime.now(clock))
            }

            LessonResponse.from(lesson)
        }!!

    private data class PreparedDeleteSchedule(
        val calendarEvent: CalendarEventDelete? = null,
    )

    private data class CalendarEventDelete(
        val eventId: String,
        val calendarClient: CentralGoogleCalendarClient,
        val refreshToken: String,
    )
}
