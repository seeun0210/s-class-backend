package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.ErrorCode

enum class OAuthErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    OAUTH_PROVIDER_ERROR("OAUTH_001", "OAuth 프로바이더 요청 실패", 502),
    OAUTH_USER_INFO_FAILED("OAUTH_002", "사용자 정보 조회 실패", 502),
    OAUTH_UNSUPPORTED_PROVIDER("OAUTH_003", "지원하지 않는 OAuth 프로바이더", 400),
    OAUTH_EMAIL_NOT_PROVIDED("OAUTH_004", "OAuth 프로바이더에서 이메일을 제공하지 않았습니다", 400),
}
