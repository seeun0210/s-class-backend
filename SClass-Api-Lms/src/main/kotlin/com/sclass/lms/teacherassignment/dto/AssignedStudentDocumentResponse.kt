package com.sclass.lms.teacherassignment.dto

import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType

data class AssignedStudentDocumentResponse(
    val id: String,
    val documentType: StudentDocumentType,
    val file: AssignedStudentDocumentFileResponse,
) {
    companion object {
        fun from(document: StudentDocument) =
            AssignedStudentDocumentResponse(
                id = document.id,
                documentType = document.documentType,
                file = AssignedStudentDocumentFileResponse.from(document.file),
            )
    }
}
