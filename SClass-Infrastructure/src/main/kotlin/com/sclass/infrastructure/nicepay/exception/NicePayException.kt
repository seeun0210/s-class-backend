package com.sclass.infrastructure.nicepay.exception

class NicePayException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
