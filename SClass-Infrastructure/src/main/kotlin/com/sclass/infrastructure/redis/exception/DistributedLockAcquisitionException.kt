package com.sclass.infrastructure.redis.exception

import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode

class DistributedLockAcquisitionException(
    val key: String,
) : BusinessException(GlobalErrorCode.LOCK_CONFLICT)
