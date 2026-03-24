package com.sclass.supporters.student.dto

import com.sclass.domain.domains.user.domain.Grade
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateStudentProfileRequest(
    @field:NotNull
    val grade: Grade,

    @field:NotBlank
    val school: String,

    val parentPhoneNumber: String?,
)
