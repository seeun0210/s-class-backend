package com.sclass.infrastructure.redis.exception

import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class DistributedLockAcquisitionExceptionTest {
    @Test
    fun `분산 락 획득 실패는 409 business error로 처리된다`() {
        val exception = DistributedLockAcquisitionException("lock:teacher-google-account:user-id")

        assertInstanceOf(BusinessException::class.java, exception)
        assertEquals(GlobalErrorCode.LOCK_CONFLICT, exception.errorCode)
        assertEquals(409, exception.errorCode.httpStatus)
        assertEquals("lock:teacher-google-account:user-id", exception.key)
    }
}
