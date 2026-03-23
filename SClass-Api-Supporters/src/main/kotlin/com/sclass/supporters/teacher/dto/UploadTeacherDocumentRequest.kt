package com.sclass.supporters.teacher.dto

import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UploadTeacherDocumentRequest(
    @field:NotBlank
    val fileId: String?,

    @field:NotNull
    val documentType: TeacherDocumentType?,
)
