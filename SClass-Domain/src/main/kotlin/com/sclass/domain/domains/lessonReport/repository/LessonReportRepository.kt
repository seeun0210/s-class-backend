package com.sclass.domain.domains.lessonReport.repository

import com.sclass.domain.domains.lessonReport.domain.LessonReport
import org.springframework.data.jpa.repository.JpaRepository

interface LessonReportRepository : JpaRepository<LessonReport, Long> {
    fun findByLessonId(lessonId: Long): LessonReport?
}
