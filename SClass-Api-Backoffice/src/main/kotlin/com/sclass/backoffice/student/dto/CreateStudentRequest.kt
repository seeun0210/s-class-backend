package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateStudentRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val name: String,

    @field:NotNull
    val platform: Platform,

    @field:NotBlank
    val phoneNumber: String,

    val grade: Grade? = null,

    val school: String? = null,

    val parentPhoneNumber: String? = null,
)
