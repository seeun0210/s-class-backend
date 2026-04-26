package com.sclass.domain.domains.oauth.exception

import com.sclass.common.exception.ErrorCode

enum class OAuthErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    TEACHER_GOOGLE_ACCOUNT_NOT_FOUND("OAUTH_015", "연결된 Google 계정이 없습니다", 404),
    CENTRAL_GOOGLE_ACCOUNT_NOT_FOUND("OAUTH_022", "연결된 중앙 Google 계정이 없습니다", 404),
    CENTRAL_GOOGLE_ACCOUNT_EMAIL_NOT_ALLOWED("OAUTH_023", "허용된 중앙 Google 계정 이메일이 아닙니다", 400),
}
