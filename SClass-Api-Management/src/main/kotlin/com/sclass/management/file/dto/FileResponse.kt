package com.sclass.management.file.dto

import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import java.time.LocalDateTime

data class FileResponse(
    val id: String,
    val originalFilename: String,
    val storedFilename: String,
    val filePath: String,
    val mimeType: String,
    val fileSize: Long,
    val fileType: FileType,
    val uploadedBy: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(file: File): FileResponse =
            FileResponse(
                id = file.id,
                originalFilename = file.originalFilename,
                storedFilename = file.storedFilename,
                filePath = file.filePath,
                mimeType = file.mimeType,
                fileSize = file.fileSize,
                fileType = file.fileType,
                uploadedBy = file.uploadedBy,
                createdAt = file.createdAt,
            )
    }
}
