package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.ErrorCode

enum class UserErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    USER_NOT_FOUND("USER_001", "유저를 찾을 수 없습니다", 404),
    USER_ALREADY_EXISTS("USER_002", "이미 존재하는 이메일입니다", 409),
    ROLE_NOT_FOUND("USER_003", "해당 권한이 없습니다", 403),
    INVALID_USER_ROLE_STATE_TRANSITION("USER_004", "허용되지 않는 상태 전이입니다", 400),
    CONFLICTING_ROLE("USER_005", "같은 플랫폼에서 학생과 선생님 역할을 동시에 가질 수 없습니다", 409),
    INVALID_STATE_REQUEST("USER_006", "APPROVED 또는 REJECTED만 요청할 수 있습니다", 400),
    REJECT_REASON_REQUIRED("USER_007", "반려 사유는 필수입니다", 400),
}
