package com.sclass.supporters.inquiry.dto

import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import java.time.LocalDateTime

data class InquiryPlanResponse(
    val id: Long,
    val sourceType: InquiryPlanSourceType,
    val sourceRefId: Long,
    val status: InquiryPlanStatus,
    val topic: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(plan: InquiryPlan) =
            InquiryPlanResponse(
                id = plan.id,
                sourceType = plan.sourceType,
                sourceRefId = plan.sourceRefId,
                status = plan.status,
                topic = plan.topic,
                createdAt = plan.createdAt,
            )
    }
}
