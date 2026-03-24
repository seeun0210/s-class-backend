package com.sclass.lms.file.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.service.FileDomainService
import com.sclass.infrastructure.s3.S3Service
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteFileUseCase(
    private val fileDomainService: FileDomainService,
    private val s3Service: S3Service,
) {
    @Transactional
    fun execute(fileId: String) {
        val file = fileDomainService.delete(Ulid.parse(fileId))
        s3Service.deleteObject(file.storedFilename)
    }
}
