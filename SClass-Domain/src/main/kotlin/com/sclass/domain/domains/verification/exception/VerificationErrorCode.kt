package com.sclass.domain.domains.verification.exception

import com.sclass.common.exception.ErrorCode

enum class VerificationErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    VERIFICATION_NOT_FOUND("VERIFICATION_001", "인증 요청을 찾을 수 없습니다", 404),
    VERIFICATION_EXPIRED("VERIFICATION_002", "인증번호가 만료되었습니다", 400),
    VERIFICATION_CODE_MISMATCH("VERIFICATION_003", "인증번호가 일치하지 않습니다", 400),
    VERIFICATION_MAX_ATTEMPTS("VERIFICATION_004", "인증 시도 횟수를 초과했습니다", 429),
    VERIFICATION_SEND_RATE_LIMIT("VERIFICATION_005", "인증번호 발송 횟수를 초과했습니다", 429),
}
