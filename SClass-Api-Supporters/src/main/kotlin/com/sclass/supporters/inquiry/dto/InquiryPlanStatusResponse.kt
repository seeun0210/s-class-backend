package com.sclass.supporters.inquiry.dto

import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus

data class InquiryPlanStatusResponse(
    val id: Long,
    val status: InquiryPlanStatus,
    val topic: String?,
    val failureReason: String?,
) {
    companion object {
        fun from(plan: InquiryPlan) =
            InquiryPlanStatusResponse(
                id = plan.id,
                status = plan.status,
                topic = plan.topic,
                failureReason = plan.failureReason,
            )
    }
}
