package com.sclass.domain.domains.commission.exception

import com.sclass.common.exception.BusinessException

class CommissionUnauthorizedAccessException : BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
