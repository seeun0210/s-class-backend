package com.sclass.domain.domains.oauth.exception

import com.sclass.common.exception.ErrorCode

enum class OAuthErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    TEACHER_GOOGLE_ACCOUNT_NOT_FOUND("OAUTH_015", "연결된 Google 계정이 없습니다", 404),
}
