package com.sclass.common.exception

open class BusinessException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message) {
    override fun fillInStackTrace(): Throwable = this
}
