package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class UserAlreadyExistsException private constructor() : BusinessException(UserErrorCode.USER_ALREADY_EXISTS) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = UserAlreadyExistsException()
    }
}
