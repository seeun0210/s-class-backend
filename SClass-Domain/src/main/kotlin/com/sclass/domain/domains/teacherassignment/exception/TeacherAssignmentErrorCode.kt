package com.sclass.domain.domains.teacherassignment.exception

import com.sclass.common.exception.ErrorCode

enum class TeacherAssignmentErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    TEACHER_ASSIGNMENT_NOT_FOUND("TEACHER_ASSIGNMENT_001", "담당 선생님 배정을 찾을 수 없습니다", 404),
    ORGANIZATION_REQUIRED_FOR_LMS("TEACHER_ASSIGNMENT_002", "LMS 플랫폼은 기관 ID가 필수입니다", 400),
    ORGANIZATION_NOT_ALLOWED_FOR_SUPPORTERS("TEACHER_ASSIGNMENT_003", "SUPPORTERS 플랫폼은 기관 ID를 지정할 수 없습니다", 400),
}
