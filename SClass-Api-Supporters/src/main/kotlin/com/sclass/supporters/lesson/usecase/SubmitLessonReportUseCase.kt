package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReport
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.domain.domains.lessonReport.exception.LessonReportAlreadyReportedException
import com.sclass.supporters.lesson.dto.LessonReportResponse
import com.sclass.supporters.lesson.dto.SubmitLessonReportRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class SubmitLessonReportUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val lessonReportAdaptor: LessonReportAdaptor,
    private val lessonReportFileAdaptor: LessonReportFileAdaptor,
    private val fileAdaptor: FileAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        lessonId: Long,
        request: SubmitLessonReportRequest,
    ): LessonReportResponse {
        val lesson = lessonAdaptor.findById(lessonId)

        if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()

        when (lesson.status) {
            LessonStatus.SCHEDULED, LessonStatus.IN_PROGRESS -> {
                lesson.complete(userId)
                lessonAdaptor.save(lesson)
            }
            LessonStatus.COMPLETED -> Unit
            LessonStatus.CANCELLED -> throw LessonInvalidStatusTransitionException()
        }

        lessonReportAdaptor.findByLessonOrNull(lessonId)?.let { throw LessonReportAlreadyReportedException() }

        val report =
            lessonReportAdaptor.save(
                LessonReport(
                    lessonId = lessonId,
                    submittedByUserId = userId,
                    content = request.content,
                ),
            )

        val savedFileIds =
            if (request.fileIds.isEmpty()) {
                emptyList()
            } else {
                val files = fileAdaptor.findAllByIds(request.fileIds)
                val reportFiles = files.map { LessonReportFile(lessonReport = report, file = it) }
                lessonReportFileAdaptor.saveAll(reportFiles).map { it.file.id }
            }

        return LessonReportResponse.of(report, savedFileIds)
    }
}
