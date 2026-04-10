package com.sclass.domain.domains.lesson.exception

import com.sclass.common.exception.ErrorCode

enum class LessonErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    LESSON_NOT_FOUND("LESSON_001", "수업을 찾을 수 없습니다", 404),
    LESSON_INVALID_STATUS_TRANSITION("LESSON_002", "잘못된 수업 상태 전이입니다", 400),
    LESSON_NOT_COMPLETED("LESSON_003", "완료되지 않은 수업입니다", 400),
    LESSON_ALREADY_REPORTED("LESSON_004", "이미 리포트가 제출되었습니다", 409),

    LESSON_REPORT_NOT_FOUND("LESSON_005", "수업 리포트를 찾을 수 없습니다", 404),
    LESSON_REPORT_NOT_PENDING("LESSON_006", "검토 대기 상태의 리포트가 아닙니다", 400),
    LESSON_REPORT_ALREADY_APPROVED("LESSON_007", "이미 승인된 리포트입니다", 409),
    LESSON_REPORT_INVALID_STATUS_TRANSITION("LESSON_008", "잘못된 리포트 상태 전이입니다", 400),
    LESSON_REPORT_REJECT_REASON_REQUIRED("LESSON_009", "반려 사유가 필요합니다", 400),
}
