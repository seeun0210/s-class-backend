package com.sclass.supporters.teacher.dto

import com.sclass.domain.domains.teacher.domain.MajorCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class UpdateTeacherProfileRequest(
    @field:NotNull
    val birthDate: LocalDate?,

    val selfIntroduction: String?,

    @field:NotNull
    val majorCategory: MajorCategory?,

    @field:NotBlank
    val university: String?,

    @field:NotBlank
    val major: String?,

    @field:NotBlank
    val highSchool: String?,

    @field:NotBlank
    val address: String?,

    @field:NotBlank
    val residentNumber: String?,
)
