package com.sclass.domain.domains.diagnosis.exception

import com.sclass.common.exception.ErrorCode

enum class DiagnosisErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    DIAGNOSIS_NOT_FOUND("DIAGNOSIS_001", "진단 결과를 찾을 수 없습니다", 404),
    DIAGNOSIS_PHONE_NOT_MATCH(
        "DIAGNOSIS_002",
        "등록된 전화번호와 일치하지 않습니다",
        403,
    ),
}
