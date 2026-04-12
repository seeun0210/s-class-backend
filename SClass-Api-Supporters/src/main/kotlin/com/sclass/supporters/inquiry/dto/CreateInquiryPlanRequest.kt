package com.sclass.supporters.inquiry.dto

import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import jakarta.validation.constraints.NotBlank

data class CreateInquiryPlanRequest(
    @field:NotBlank val paragraph: String,
    val sourceType: InquiryPlanSourceType,
    val sourceRefId: Long,
)
