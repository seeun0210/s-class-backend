package com.sclass.domain.domains.partnership.exception

import com.sclass.common.exception.ErrorCode

enum class PartnershipLeadErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    PARTNERSHIP_LEAD_NOT_FOUND("PARTNERSHIP_001", "파트너십 문의를 찾을 수 없습니다", 404),
    PARTNERSHIP_LEAD_ALREADY_EXISTS("PARTNERSHIP_002", "이미 접수된 연락처입니다. 곧 연락드리겠습니다", 409),
}
