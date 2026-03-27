package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class ConflictingRoleException : BusinessException(UserErrorCode.CONFLICTING_ROLE)
