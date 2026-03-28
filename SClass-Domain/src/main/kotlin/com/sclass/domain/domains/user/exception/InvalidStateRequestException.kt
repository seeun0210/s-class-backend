package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class InvalidStateRequestException : BusinessException(UserErrorCode.INVALID_STATE_REQUEST)
