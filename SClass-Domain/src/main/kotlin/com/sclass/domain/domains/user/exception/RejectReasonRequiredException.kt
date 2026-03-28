package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class RejectReasonRequiredException : BusinessException(UserErrorCode.REJECT_REASON_REQUIRED)
