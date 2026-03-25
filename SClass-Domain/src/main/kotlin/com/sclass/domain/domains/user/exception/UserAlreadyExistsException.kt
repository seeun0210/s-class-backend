package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class UserAlreadyExistsException : BusinessException(UserErrorCode.USER_ALREADY_EXISTS)
