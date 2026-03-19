package com.sclass.common.exception

enum class GlobalErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    INVALID_INPUT("GLOBAL_001", "잘못된 입력입니다", 400),
    UNAUTHORIZED("GLOBAL_002", "인증이 필요합니다", 401),
    FORBIDDEN("GLOBAL_003", "권한이 없습니다", 403),
    NOT_FOUND("GLOBAL_004", "리소스를 찾을 수 없습니다", 404),
    CONFLICT("GLOBAL_005", "충돌이 발생했습니다", 409),
    INTERNAL_ERROR("GLOBAL_500", "서버 내부 오류가 발생했습니다", 500),
}
