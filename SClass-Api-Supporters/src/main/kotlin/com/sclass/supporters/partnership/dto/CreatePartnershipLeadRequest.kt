package com.sclass.supporters.partnership.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreatePartnershipLeadRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val academyName: String,

    @field:NotBlank
    @field:Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "유효한 휴대폰 번호 형식이 아닙니다")
    val phone: String,

    @field:Email
    @field:Size(max = 255)
    val email: String?,

    @field:Size(max = 2000)
    val message: String?,
)
