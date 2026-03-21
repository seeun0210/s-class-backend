package com.sclass.lms.file.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File

@UseCase
class ReadFileUseCase(
    private val fileAdaptor: FileAdaptor,
) {
    fun getFile(fileId: String): File = fileAdaptor.findById(Ulid.parse(fileId))
}
