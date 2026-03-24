package com.sclass.lms.file.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.service.FileDomainService
import com.sclass.infrastructure.s3.S3Service
import com.sclass.lms.file.dto.DownloadUrlResponse

@UseCase
class GetFileDownloadUrlUseCase(
    private val fileDomainService: FileDomainService,
    private val s3Service: S3Service,
) {
    fun execute(fileId: String): DownloadUrlResponse {
        val file = fileDomainService.findById(Ulid.parse(fileId))
        val downloadUrl = s3Service.generatePresignedGetUrl(key = file.storedFilename)

        return DownloadUrlResponse(
            downloadUrl = downloadUrl,
            originalFilename = file.originalFilename,
            mimeType = file.mimeType,
        )
    }
}
