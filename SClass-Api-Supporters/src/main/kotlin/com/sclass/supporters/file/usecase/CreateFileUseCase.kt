package com.sclass.supporters.file.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.file.service.FileService
import com.sclass.infrastructure.s3.S3Service
import com.sclass.supporters.file.dto.PresignedUrlResponse
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@UseCase
class CreateFileUseCase(
    private val s3Service: S3Service,
    private val fileService: FileService,
) {
    @Transactional
    fun execute(
        uploadedBy: String,
        originalFilename: String,
        contentType: String,
        fileSize: Long,
        fileType: FileType,
    ): PresignedUrlResponse {
        val fileId = Ulid.generate()
        val storedFilename = "${fileId}_$originalFilename"
        val directoryPath = generateDirectoryPath(fileType)
        val filePath = "$directoryPath/$storedFilename"

        val file =
            File.create(
                id = fileId,
                originalFilename = originalFilename,
                storedFilename = storedFilename,
                filePath = filePath,
                mimeType = contentType,
                fileSize = fileSize,
                fileType = fileType,
                uploadedBy = uploadedBy,
            )
        fileService.save(file)

        val presignedUrl =
            s3Service.generatePresignedPutUrl(
                key = filePath,
                contentType = contentType,
            )

        return PresignedUrlResponse(
            fileId = fileId,
            presignedUrl = presignedUrl,
            filePath = filePath,
        )
    }

    private fun generateDirectoryPath(fileType: FileType): String {
        val today = LocalDate.now()
        val datePath = today.format(DateTimeFormatter.ofPattern("yyyy/MM"))
        return "supporters/${fileType.name.lowercase()}/$datePath"
    }
}
