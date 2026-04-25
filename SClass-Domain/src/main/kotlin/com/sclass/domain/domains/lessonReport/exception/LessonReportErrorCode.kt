package com.sclass.domain.domains.lessonReport.exception

import com.sclass.common.exception.ErrorCode

enum class LessonReportErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    LESSON_REPORT_NOT_FOUND("LESSON_REPORT_001", "수업 리포트를 찾을 수 없습니다", 404),
    LESSON_REPORT_NOT_PENDING("LESSON_REPORT_002", "검토 대기 상태의 리포트가 아닙니다", 400),
    LESSON_REPORT_ALREADY_APPROVED("LESSON_REPORT_003", "이미 승인된 리포트입니다", 409),
    LESSON_REPORT_INVALID_STATUS_TRANSITION("LESSON_REPORT_004", "잘못된 리포트 상태 전이입니다", 400),
    LESSON_REPORT_REJECT_REASON_REQUIRED("LESSON_REPORT_005", "반려 사유가 필요합니다", 400),
    LESSON_REPORT_ALREADY_REPORTED("LESSON_REPORT_006", "이미 리포트가 제출되었습니다", 409),
    LESSON_REPORT_NOT_REJECTED("LESSON_REPORT_007", "반려된 리포트만 수정할 수 있습니다", 400),
}
