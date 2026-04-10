package com.sclass.domain.domains.lesson.repository

import com.sclass.domain.domains.lesson.domain.LessonReport
import com.sclass.domain.domains.lesson.domain.LessonReportStatus
import org.springframework.data.jpa.repository.JpaRepository

interface LessonReportRepository : JpaRepository<LessonReport, Long> {
    fun findAllByLessonIdOrderByVersionDesc(lessonId: Long): List<LessonReport>

    fun findTopByLessonIdOrderByVersionDesc(lessonId: Long): LessonReport?

    fun findAllByLessonIdAndStatus(
        lessonId: Long,
        status: LessonReportStatus,
    ): List<LessonReport>

    fun countByLessonId(lessonId: Long): Int
}
