package com.sclass.domain.domains.inquiryplan.exception

import com.sclass.common.exception.ErrorCode

enum class InquiryPlanErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    INQUIRY_PLAN_NOT_FOUND("INQUIRY_001", "탐구계획을 찾을 수 없습니다", 404),
}
