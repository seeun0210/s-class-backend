package com.sclass.common.jwt.exception

import com.sclass.common.exception.BusinessException

class InvalidTokenException private constructor() : BusinessException(TokenErrorCode.INVALID_TOKEN) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = InvalidTokenException()
    }
}
