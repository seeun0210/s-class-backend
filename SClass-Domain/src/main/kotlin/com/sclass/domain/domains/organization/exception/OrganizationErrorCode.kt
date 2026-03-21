package com.sclass.domain.domains.organization.exception

import com.sclass.common.exception.ErrorCode

enum class OrganizationErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    ORGANIZATION_NOT_FOUND("ORGANIZATION_001", "기관을 찾을 수 없습니다", 404),
}
