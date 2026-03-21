package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class RoleNotFoundException private constructor() : BusinessException(UserErrorCode.ROLE_NOT_FOUND) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = RoleNotFoundException()
    }
}
