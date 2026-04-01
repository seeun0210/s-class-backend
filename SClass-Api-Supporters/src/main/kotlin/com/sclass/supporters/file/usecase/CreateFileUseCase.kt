package com.sclass.supporters.file.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.infrastructure.s3.S3Service
import com.sclass.supporters.file.dto.PresignedUrlResponse
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@UseCase
class CreateFileUseCase(
    private val s3Service: S3Service,
    private val fileAdaptor: FileAdaptor,
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
        val directoryPath = generateDirectoryPath(fileType)
        val storedFilename = "$directoryPath/${fileId}_$originalFilename"

        val file =
            File.create(
                id = fileId,
                originalFilename = originalFilename,
                storedFilename = storedFilename,
                mimeType = contentType,
                fileSize = fileSize,
                fileType = fileType,
                uploadedBy = uploadedBy,
            )
        fileAdaptor.save(file)

        val presignedUrl =
            s3Service.generatePresignedPutUrl(
                key = storedFilename,
                contentType = contentType,
            )

        return PresignedUrlResponse(
            fileId = fileId,
            presignedUrl = presignedUrl,
            storedFilename = storedFilename,
        )
    }

    private fun generateDirectoryPath(fileType: FileType): String {
        val today = LocalDate.now()
        val datePath = today.format(DateTimeFormatter.ofPattern("yyyy/MM"))
        return "supporters/${fileType.name.lowercase()}/$datePath"
    }
}
