package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.ErrorCode

enum class UserErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    USER_NOT_FOUND("USER_001", "유저를 찾을 수 없습니다", 404),
    USER_ALREADY_EXISTS("USER_002", "이미 존재하는 이메일입니다", 409),
}
