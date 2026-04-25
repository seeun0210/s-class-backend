package com.sclass.backoffice.lessonReport.usecase

import com.sclass.backoffice.lessonReport.dto.LessonReportResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetLessonReportUseCase(
    private val lessonReportAdaptor: LessonReportAdaptor,
    private val lessonReportFileAdaptor: LessonReportFileAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(lessonId: Long): LessonReportResponse {
        val report = lessonReportAdaptor.findByLesson(lessonId)
        val fileIds = lessonReportFileAdaptor.findByLessonReportId(report.id).map { it.file.id }
        return LessonReportResponse.of(report, fileIds)
    }
}
