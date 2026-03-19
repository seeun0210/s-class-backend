package com.sclass.domain.domains.file.exception

import com.sclass.common.exception.ErrorCode

enum class FileErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    FILE_NOT_FOUND("FILE_001", "파일을 찾을 수 없습니다", 404),
}
