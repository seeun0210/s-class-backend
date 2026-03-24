package com.sclass.domain.domains.file.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File

@DomainService
class FileDomainService(
    private val fileAdaptor: FileAdaptor,
) {
    fun save(file: File): File = fileAdaptor.save(file)

    fun findById(fileId: String): File = fileAdaptor.findById(fileId)

    fun delete(fileId: String): File {
        val file = fileAdaptor.findById(fileId)
        fileAdaptor.delete(fileId)
        return file
    }
}
