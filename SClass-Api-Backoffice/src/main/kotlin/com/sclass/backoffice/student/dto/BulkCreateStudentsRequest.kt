package com.sclass.backoffice.student.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class BulkCreateStudentsRequest(
    @field:NotEmpty
    @field:Size(max = MAX_BULK_SIZE)
    @field:Valid
    val students: List<CreateStudentRequest>,
) {
    companion object {
        const val MAX_BULK_SIZE = 100
    }
}
