package com.sclass.supporters.student.dto

import com.sclass.domain.domains.student.domain.StudentDocumentType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UploadStudentDocumentRequest(
    @field:NotBlank
    val fileId: String?,

    @field:NotNull
    val documentType: StudentDocumentType?,
)
