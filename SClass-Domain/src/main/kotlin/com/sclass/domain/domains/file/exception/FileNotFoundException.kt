package com.sclass.domain.domains.file.exception

import com.sclass.common.exception.BusinessException

class FileNotFoundException : BusinessException(FileErrorCode.FILE_NOT_FOUND)
