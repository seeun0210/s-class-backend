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
    LESSON_UNAUTHORIZED_ACCESS("LESSON_004", "해당 수업에 대한 권한이 없습니다", 403),
}
