package com.sclass.lms.teacherassignment.dto

import com.sclass.domain.domains.file.domain.File

data class AssignedStudentDocumentFileResponse(
    val id: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
) {
    companion object {
        fun from(file: File) =
            AssignedStudentDocumentFileResponse(
                id = file.id,
                originalFilename = file.originalFilename,
                mimeType = file.mimeType,
                fileSize = file.fileSize,
            )
    }
}
