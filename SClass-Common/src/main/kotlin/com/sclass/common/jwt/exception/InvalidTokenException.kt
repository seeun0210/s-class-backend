package com.sclass.common.jwt.exception

import com.sclass.common.exception.BusinessException

class InvalidTokenException : BusinessException(TokenErrorCode.INVALID_TOKEN)
