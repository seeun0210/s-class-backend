package com.sclass.domain.domains.file.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.repository.FileRepository
import org.springframework.transaction.annotation.Transactional

@DomainService
@Transactional(readOnly = true)
class FileService(
    private val fileRepository: FileRepository,
) {
    @Transactional
    fun save(file: File): File = fileRepository.save(file)

    @Transactional
    fun delete(id: String) {
        fileRepository.deleteById(id)
    }
}
