package com.sclass.common.jwt.exception

import com.sclass.common.exception.ErrorCode

enum class TokenErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    TOKEN_EXPIRED("TOKEN_001", "토큰이 만료되었습니다", 401),
    INVALID_TOKEN("TOKEN_002", "유효하지 않은 토큰입니다", 401),
    REFRESH_TOKEN_EXPIRED("TOKEN_003", "리프레시 토큰이 만료되었습니다", 401),
    REFRESH_TOKEN_REVOKED("TOKEN_004", "이미 폐기된 리프레시 토큰입니다", 401),
}
