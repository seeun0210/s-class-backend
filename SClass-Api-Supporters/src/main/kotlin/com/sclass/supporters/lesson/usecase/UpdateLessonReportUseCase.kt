package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.supporters.lesson.dto.LessonReportResponse
import com.sclass.supporters.lesson.dto.UpdateLessonReportRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateLessonReportUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val lessonReportAdaptor: LessonReportAdaptor,
    private val lessonReportFileAdaptor: LessonReportFileAdaptor,
    private val fileAdaptor: FileAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        lessonId: Long,
        request: UpdateLessonReportRequest,
    ): LessonReportResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()

        val report = lessonReportAdaptor.findByLesson(lessonId)
        report.resubmit(request.content)

        lessonReportFileAdaptor.deleteAllByLessonReportId(report.id)

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
