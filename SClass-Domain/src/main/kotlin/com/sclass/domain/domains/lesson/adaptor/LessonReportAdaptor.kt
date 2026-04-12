package com.sclass.domain.domains.lesson.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.lesson.domain.LessonReport
import com.sclass.domain.domains.lesson.exception.LessonReportNotFoundException
import com.sclass.domain.domains.lesson.repository.LessonReportRepository
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class LessonReportAdaptor(
    private val lessonReportRepository: LessonReportRepository,
) {
    fun save(report: LessonReport): LessonReport = lessonReportRepository.save(report)

    fun findById(id: Long): LessonReport = lessonReportRepository.findByIdOrNull(id) ?: throw LessonReportNotFoundException()

    fun findAllByLesson(lessonId: Long): List<LessonReport> = lessonReportRepository.findAllByLessonIdOrderByVersionDesc(lessonId)

    fun findLatestByLesson(lessonId: Long): LessonReport? = lessonReportRepository.findTopByLessonIdOrderByVersionDesc(lessonId)

    fun nextVersion(lessonId: Long): Int = (findLatestByLesson(lessonId)?.version ?: 0) + 1
}
