package com.sclass.domain.domains.lessonReport.repository

import com.sclass.domain.domains.lessonReport.domain.LessonReportFile

interface LessonReportFileCustomRepository {
    fun findByLessonReportId(lessonReportId: Long): List<LessonReportFile>

    fun findByLessonReportIds(lessonReportIds: Collection<Long>): List<LessonReportFile>
}
