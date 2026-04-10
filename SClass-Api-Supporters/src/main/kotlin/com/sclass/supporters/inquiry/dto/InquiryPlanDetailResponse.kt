package com.sclass.supporters.inquiry.dto

import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import com.sclass.infrastructure.report.dto.ReportStateDto
import java.time.LocalDateTime

data class InquiryPlanDetailResponse(
    val id: Long,
    val sourceType: InquiryPlanSourceType,
    val sourceRefId: Long,
    val status: InquiryPlanStatus,
    val topic: String?,
    val report: ReportStateDto?,
    val failureReason: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            plan: InquiryPlan,
            report: ReportStateDto?,
        ) = InquiryPlanDetailResponse(
            id = plan.id,
            sourceType = plan.sourceType,
            sourceRefId = plan.sourceRefId,
            status = plan.status,
            topic = plan.topic,
            report = report,
            failureReason = plan.failureReason,
            createdAt = plan.createdAt,
        )
    }
}
