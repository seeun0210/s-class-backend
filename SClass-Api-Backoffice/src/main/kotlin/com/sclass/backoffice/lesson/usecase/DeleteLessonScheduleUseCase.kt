package com.sclass.backoffice.lesson.usecase

import com.sclass.backoffice.lesson.dto.LessonResponse
import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonScheduleNotFoundException
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.infrastructure.calendar.CentralGoogleCalendarClient
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
    fun execute(lessonId: Long): LessonResponse =
        txTemplate.execute {
            val lesson = lessonAdaptor.findByIdForUpdate(lessonId)
            if (lesson.scheduledAt == null) throw LessonScheduleNotFoundException()
            lesson.validateScheduleUpdatable()

            val eventId = lesson.googleMeet?.calendarEventId?.takeIf { it.isNotBlank() }
            val centralAccount =
                eventId?.let {
                    val calendarClient =
                        centralGoogleCalendarClientProvider.getIfAvailable()
                            ?: throw GoogleCalendarCentralDisabledException()
                    val account = centralGoogleAccountAdaptor.findGoogle()
                    val refreshToken = aesTokenEncryptor.decrypt(account.encryptedRefreshToken)

                    calendarClient.deleteMeetEventWithRefreshToken(
                        refreshToken = refreshToken,
                        eventId = it,
                        sendUpdates = true,
                    )
                    account
                }

            lesson.deleteSchedule()
            lessonAdaptor.save(lesson)

            centralAccount?.let {
                it.markUsed(LocalDateTime.now(clock))
                centralGoogleAccountAdaptor.save(it)
            }

            LessonResponse.from(lesson)
        }!!
}
