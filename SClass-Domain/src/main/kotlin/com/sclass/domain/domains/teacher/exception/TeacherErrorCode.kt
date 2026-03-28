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
    TEACHER_NOT_EDITABLE("TEACHER_004", "현재 상태에서는 프로필을 수정할 수 없습니다", 400),
    TEACHER_NOT_SUBMITTABLE("TEACHER_005", "현재 상태에서는 승인 요청을 할 수 없습니다", 400),
    TEACHER_PROFILE_INCOMPLETE("TEACHER_006", "필수 프로필 정보가 모두 입력되지 않았습니다", 400),
    TEACHER_REQUIRED_DOCUMENTS_MISSING("TEACHER_007", "필수 서류가 모두 업로드되지 않았습니다", 400),
    TEACHER_NOT_PENDING("TEACHER_008", "승인 대기 상태가 아닙니다", 400),
    TEACHER_REJECT_REASON_REQUIRED("TEACHER_009", "반려 사유는 필수입니다", 400),
    TEACHER_INVALID_VERIFICATION_STATUS("TEACHER_010", "승인 또는 반려 상태만 지정할 수 있습니다", 400),
    TEACHER_CONTRACT_DATE_INVALID("TEACHER_011", "계약 시작일은 종료일보다 이전이어야 합니다", 400),
}
