package com.sclass.domain.domains.file.exception

import com.sclass.common.exception.BusinessException

class FileNotFoundException private constructor() : BusinessException(FileErrorCode.FILE_NOT_FOUND) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = FileNotFoundException()
    }
}
