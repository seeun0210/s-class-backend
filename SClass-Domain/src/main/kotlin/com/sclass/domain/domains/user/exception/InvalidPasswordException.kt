package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class InvalidPasswordException : BusinessException(AuthErrorCode.INVALID_PASSWORD)
