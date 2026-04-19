package com.sclass.backoffice.coin.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class AdminGrantCoinRequest(
    @field:NotBlank val studentUserId: String,
    @field:Min(1) val amount: Int,
    @field:NotBlank val grantReason: String,
    val enrollmentId: Long? = null,
    val expireAt: LocalDateTime? = null,
)
