package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class DuplicateUserRoleException : BusinessException(UserErrorCode.DUPLICATE_USER_ROLE)
