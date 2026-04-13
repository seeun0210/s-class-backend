package com.sclass.backoffice.lessonReport.usecase

import com.sclass.backoffice.lessonReport.dto.LessonReportResponse
import com.sclass.backoffice.lessonReport.dto.ReviewDecision
import com.sclass.backoffice.lessonReport.dto.ReviewLessonReportRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.exception.LessonReportRejectReasonRequiredException
import org.springframework.transaction.annotation.Transactional

@UseCase
class ReviewLessonReportUseCase(
    private val lessonReportAdaptor: LessonReportAdaptor,
    private val lessonReportFileAdaptor: LessonReportFileAdaptor,
) {
    @Transactional
    fun execute(
        reviewerUserId: String,
        reportId: Long,
        request: ReviewLessonReportRequest,
    ): LessonReportResponse {
        val report = lessonReportAdaptor.findById(reportId)
        when (request.decision) {
            ReviewDecision.APPROVE -> report.approve(reviewerUserId)
            ReviewDecision.REJECT -> {
                val reason =
                    request.rejectReason?.takeIf { it.isNotBlank() }
                        ?: throw LessonReportRejectReasonRequiredException()
                report.reject(reviewerUserId, reason)
            }
        }
        val fileIds = lessonReportFileAdaptor.findByLessonReportId(reportId).map { it.file.id }
        return LessonReportResponse.of(report, fileIds)
    }
}
