package com.sclass.domain.domains.lessonReport.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReport
import com.sclass.domain.domains.lessonReport.exception.LessonReportNotFoundException
import com.sclass.domain.domains.lessonReport.repository.LessonReportRepository
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class LessonReportAdaptor(
    private val lessonReportRepository: LessonReportRepository,
) {
    fun save(report: LessonReport): LessonReport = lessonReportRepository.save(report)

    fun findById(id: Long): LessonReport = lessonReportRepository.findByIdOrNull(id) ?: throw LessonReportNotFoundException()

    fun findByLesson(lessonId: Long): LessonReport =
        lessonReportRepository.findByLessonId(lessonId) ?: throw LessonReportNotFoundException()

    fun findByLessonOrNull(lessonId: Long): LessonReport? = lessonReportRepository.findByLessonId(lessonId)
}
