package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.supporters.lesson.dto.LessonReportResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetLessonReportsUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val lessonReportAdaptor: LessonReportAdaptor,
    private val lessonReportFileAdaptor: LessonReportFileAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        lessonId: Long,
    ): List<LessonReportResponse> {
        val lesson = lessonAdaptor.findById(lessonId)
        if (lesson.studentUserId != userId && !lesson.isTeacher(userId)) {
            throw LessonUnauthorizedAccessException()
        }

        val reports = lessonReportAdaptor.findAllByLesson(lessonId)
        if (reports.isEmpty()) return emptyList()

        val filesByReport =
            lessonReportFileAdaptor
                .findByLessonReportIds(reports.map { it.id })
                .groupBy({ it.lessonReport.id }, { it.file.id })

        return reports.map { LessonReportResponse.of(it, filesByReport[it.id] ?: emptyList()) }
    }
}
