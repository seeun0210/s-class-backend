package com.sclass.domain.domains.commission.exception

import com.sclass.common.exception.BusinessException

class CommissionNotFoundException : BusinessException(CommissionErrorCode.COMMISSION_NOT_FOUND)
