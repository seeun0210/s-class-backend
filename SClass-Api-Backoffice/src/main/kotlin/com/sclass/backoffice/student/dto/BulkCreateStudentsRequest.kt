package com.sclass.backoffice.student.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class BulkCreateStudentsRequest(
    @field:NotEmpty
    @field:Size(max = 100)
    @field:Valid
    val students: List<CreateStudentRequest>,
)
