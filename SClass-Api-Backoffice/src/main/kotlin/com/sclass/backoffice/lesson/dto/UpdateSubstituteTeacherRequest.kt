package com.sclass.backoffice.lesson.dto

import jakarta.validation.constraints.Size

data class UpdateSubstituteTeacherRequest(
    @field:Size(min = 26, max = 26)
    val teacherUserId: String?,
)
