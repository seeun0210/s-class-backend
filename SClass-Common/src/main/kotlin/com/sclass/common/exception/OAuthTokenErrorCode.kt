package com.sclass.common.exception

enum class OAuthTokenErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    OAUTH_TOKEN_AUDIENCE_MISMATCH("OAUTH_010", "OAuth 토큰의 대상 애플리케이션이 일치하지 않습니다", 401),
    OAUTH_TOKEN_VALIDATION_FAILED("OAUTH_011", "OAuth 토큰 검증에 실패했습니다", 502),
}
