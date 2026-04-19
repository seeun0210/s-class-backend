package com.sclass.domain.domains.product.domain

import java.time.LocalDateTime

data class ActivePeriod(
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
)
