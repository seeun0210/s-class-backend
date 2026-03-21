package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class RoleNotFoundException : BusinessException(UserErrorCode.ROLE_NOT_FOUND)
