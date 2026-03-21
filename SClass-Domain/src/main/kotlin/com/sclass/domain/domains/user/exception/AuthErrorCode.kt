package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.ErrorCode

enum class AuthErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    INVALID_PASSWORD("AUTH_001", "비밀번호가 일치하지 않습니다", 401),
    PASSWORD_NOT_SET("AUTH_002", "비밀번호가 설정되지 않은 계정입니다", 400),
}
