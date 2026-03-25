package com.sclass.domain.domains.organization.exception

import com.sclass.common.exception.ErrorCode

enum class OrganizationErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    ORGANIZATION_NOT_FOUND("ORGANIZATION_001", "기관을 찾을 수 없습니다", 404),
    ORGANIZATION_ATTRIBUTION_NOT_FOUND("ORGANIZATION_002", "기관 귀속 정보를 찾을 수 없습니다", 404),
    ORGANIZATION_ALREADY_ATTRIBUTED("ORGANIZATION_003", "이미 다른 기관에 귀속되어 있습니다", 409),
    ORGANIZATION_SUBDOMAIN_NOT_RESOLVED("ORGANIZATION_004", "요청에서 기관 서브도메인을 확인할 수 없습니다", 400),
    ORGANIZATION_USER_NOT_FOUND("ORGANIZATION_005", "기관 소속 유저를 찾을 수 없습니다", 404),
}
