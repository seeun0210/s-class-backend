package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class UserNotFoundException : BusinessException(UserErrorCode.USER_NOT_FOUND)
