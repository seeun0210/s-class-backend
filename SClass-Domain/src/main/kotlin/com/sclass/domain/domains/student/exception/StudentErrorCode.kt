package com.sclass.domain.domains.student.exception

import com.sclass.common.exception.ErrorCode

enum class StudentErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    STUDENT_NOT_FOUND("STUDENT_001", "학생을 찾을 수 없습니다", 404),
    STUDENT_ALREADY_EXISTS("STUDENT_002", "이미 해당 기관에 등록된 학생입니다", 409),
    STUDENT_DOCUMENT_NOT_FOUND("STUDENT_003", "학생 서류를 찾을 수 없습니다", 404),
}
