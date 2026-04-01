package com.sclass.supporters.file.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.infrastructure.s3.S3Service
import com.sclass.supporters.file.dto.DownloadUrlResponse

@UseCase
class GetFileDownloadUrlUseCase(
    private val fileAdaptor: FileAdaptor,
    private val s3Service: S3Service,
) {
    fun execute(fileId: String): DownloadUrlResponse {
        val file = fileAdaptor.findById(Ulid.parse(fileId))
        val downloadUrl = s3Service.generatePresignedGetUrl(key = file.storedFilename)

        return DownloadUrlResponse(
            downloadUrl = downloadUrl,
            originalFilename = file.originalFilename,
            mimeType = file.mimeType,
        )
    }
}
