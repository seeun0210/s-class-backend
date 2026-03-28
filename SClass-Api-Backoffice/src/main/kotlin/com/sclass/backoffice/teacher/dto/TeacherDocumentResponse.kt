package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType

data class TeacherDocumentResponse(
    val id: String,
    val fileId: String,
    val documentType: TeacherDocumentType,
) {
    companion object {
        fun from(document: TeacherDocument): TeacherDocumentResponse =
            TeacherDocumentResponse(
                id = document.id,
                fileId = document.file.id,
                documentType = document.documentType,
            )
    }
}
