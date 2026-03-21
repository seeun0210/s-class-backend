package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class InvalidPasswordException private constructor() : BusinessException(AuthErrorCode.INVALID_PASSWORD) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = InvalidPasswordException()
    }
}
