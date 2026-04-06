package com.sclass.infrastructure.redis.exception

class DistributedLockAcquisitionException(
    key: String,
) : RuntimeException("분산 락 획득에 실패했습니다: key=$key")
