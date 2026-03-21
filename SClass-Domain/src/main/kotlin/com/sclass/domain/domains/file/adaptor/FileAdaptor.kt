package com.sclass.domain.domains.file.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.exception.FileNotFoundException
import com.sclass.domain.domains.file.repository.FileRepository

@Adaptor
class FileAdaptor(
    private val fileRepository: FileRepository,
) {
    fun findById(id: String): File = fileRepository.findById(id).orElseThrow { FileNotFoundException() }

    fun findByIdOrNull(id: String): File? = fileRepository.findById(id).orElse(null)

    fun findAllByIds(ids: List<String>): List<File> = fileRepository.findAllByIdIn(ids)
}
