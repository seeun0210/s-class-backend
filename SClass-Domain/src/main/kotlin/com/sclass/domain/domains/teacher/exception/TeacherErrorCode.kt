package com.sclass.domain.domains.teacher.exception

import com.sclass.common.exception.ErrorCode

enum class TeacherErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    TEACHER_NOT_FOUND("TEACHER_001", "교사를 찾을 수 없습니다", 404),
    TEACHER_ALREADY_EXISTS("TEACHER_002", "이미 해당 기관에 등록된 교사입니다", 409),
    TEACHER_DOCUMENT_NOT_FOUND("TEACHER_003", "교사 서류를 찾을 수 없습니다", 404),
}
