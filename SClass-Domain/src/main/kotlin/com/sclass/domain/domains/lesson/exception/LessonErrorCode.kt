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
    LESSON_SUBSTITUTE_ASSIGN_NOT_ALLOWED("LESSON_005", "예정 상태의 수업만 대타 배정이 가능합니다", 400),
    LESSON_SUBSTITUTE_SAME_AS_ASSIGNED("LESSON_006", "기존 담당 선생님은 대타로 배정할 수 없습니다", 400),
    LESSON_ALREADY_COMPLETED("LESSON_007", "이미 완료 처리된 수업입니다", 400),
    LESSON_INVALID_TIME("LESSON_008", "유효하지 않은 시간입니다", 400),
    LESSON_ALREADY_STARTED("LESSON_009", "이미 시작된 수업입니다", 400),
}
