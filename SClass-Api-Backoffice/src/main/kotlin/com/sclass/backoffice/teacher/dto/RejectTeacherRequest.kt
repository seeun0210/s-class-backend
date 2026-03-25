package com.sclass.backoffice.teacher.dto

import jakarta.validation.constraints.NotBlank

data class RejectTeacherRequest(
    @field:NotBlank
    val reason: String,
)
