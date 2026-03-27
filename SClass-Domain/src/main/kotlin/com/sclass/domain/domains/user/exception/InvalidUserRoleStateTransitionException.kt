package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class InvalidUserRoleStateTransitionException : BusinessException(UserErrorCode.INVALID_USER_ROLE_STATE_TRANSITION)
