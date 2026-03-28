package com.sclass.backoffice.student.dto

import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType

data class StudentDocumentResponse(
    val id: String,
    val fileId: String,
    val documentType: StudentDocumentType,
) {
    companion object {
        fun from(document: StudentDocument): StudentDocumentResponse =
            StudentDocumentResponse(
                id = document.id,
                fileId = document.file.id,
                documentType = document.documentType,
            )
    }
}
