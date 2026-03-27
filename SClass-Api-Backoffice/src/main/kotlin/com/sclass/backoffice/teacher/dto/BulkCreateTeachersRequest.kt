package com.sclass.backoffice.teacher.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class BulkCreateTeachersRequest(
    @field:NotEmpty
    @field:Size(max = 100)
    @field:Valid
    val teachers: List<CreateTeacherRequest>,
)
