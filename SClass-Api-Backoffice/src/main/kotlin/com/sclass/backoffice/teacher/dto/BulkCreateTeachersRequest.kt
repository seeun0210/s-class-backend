package com.sclass.backoffice.teacher.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class BulkCreateTeachersRequest(
    @field:NotEmpty
    @field:Valid
    val teachers: List<CreateTeacherRequest>,
)
