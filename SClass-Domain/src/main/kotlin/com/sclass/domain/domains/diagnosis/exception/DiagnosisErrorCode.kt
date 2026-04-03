package com.sclass.domain.domains.diagnosis.exception

import com.sclass.common.exception.ErrorCode

enum class DiagnosisErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    DIAGNOSIS_NOT_FOUND("DIAGNOSIS_001", "진단 결과를 찾을 수 없습니다", 404),
}
