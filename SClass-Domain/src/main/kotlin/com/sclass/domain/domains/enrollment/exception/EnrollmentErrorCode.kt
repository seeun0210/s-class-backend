package com.sclass.domain.domains.enrollment.exception

import com.sclass.common.exception.ErrorCode

enum class EnrollmentErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    ENROLLMENT_NOT_FOUND("ENROLLMENT_001", "수강 등록을 찾을 수 없습니다", 404),
    ENROLLMENT_ALREADY_EXISTS("ENROLLMENT_002", "이미 등록된 코스입니다", 409),
    ENROLLMENT_PAYMENT_REQUIRED("ENROLLMENT_003", "결제가 완료되지 않은 수강 등록입니다", 400),
    ENROLLMENT_INVALID_STATUS_TRANSITION("ENROLLMENT_004", "잘못된 수강 등록 상태 전이입니다", 400),
    ENROLLMENT_DUPLICATE_PAYMENT("ENROLLMENT_005", "이미 처리된 결제입니다", 409),
    ENROLLMENT_NOT_ACTIVE("ENROLLMENT_006", "활성 상태의 수강 등록이 아닙니다", 400),
    ENROLLMENT_TYPE_MISMATCH("ENROLLMENT_007", "해당 작업은 이 등록 유형에서 허용되지 않습니다", 400),
    ENROLLMENT_UNAUTHORIZED_ACCESS("ENROLLMENT_008", "해당 수강에 대한 권한이 없습니다", 403),
    MEMBERSHIP_CAPACITY_EXCEEDED("ENROLLMENT_009", "멤버십 정원이 초과되었습니다", 409),
    NO_ACTIVE_MEMBERSHIP("ENROLLMENT_010", "활성 멤버십이 없습니다", 403),
}
