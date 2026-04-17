package com.sclass.domain.domains.commission.exception

import com.sclass.common.exception.BusinessException

class CommissionPolicyNotFoundException : BusinessException(CommissionErrorCode.COMMISSION_POLICY_NOT_FOUND)
