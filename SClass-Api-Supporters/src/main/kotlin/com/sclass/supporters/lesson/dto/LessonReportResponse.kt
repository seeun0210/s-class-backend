package com.sclass.supporters.lesson.dto

import com.sclass.domain.domains.lessonReport.domain.LessonReport
import com.sclass.domain.domains.lessonReport.domain.LessonReportStatus
import java.time.LocalDateTime

data class LessonReportResponse(
    val id: Long,
    val lessonId: Long,
    val version: Int,
    val content: String,
    val fileIds: List<String>,
    val status: LessonReportStatus,
    val rejectReason: String?,
    val submittedByUserId: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(
            report: LessonReport,
            fileIds: List<String>,
        ): LessonReportResponse =
            LessonReportResponse(
                id = report.id,
                lessonId = report.lessonId,
                version = report.version,
                content = report.content,
                fileIds = fileIds,
                status = report.status,
                rejectReason = report.rejectReason,
                submittedByUserId = report.submittedByUserId,
                createdAt = report.createdAt,
            )
    }
}
