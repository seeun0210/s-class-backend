package com.sclass.supporters.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.service.FileDomainService
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.UploadTeacherDocumentRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UploadTeacherDocumentUseCase(
    private val teacherDomainService: TeacherDomainService,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
    private val fileDomainService: FileDomainService,
) {
    @Transactional
    fun execute(
        userId: String,
        request: UploadTeacherDocumentRequest,
    ): TeacherDocumentResponse {
        val teacher = teacherDomainService.findByUserId(userId)
        val file = fileDomainService.findById(request.fileId)

        val existing = teacherDocumentAdaptor.findByTeacherIdAndDocumentType(teacher.id, request.documentType)
        if (existing != null) {
            teacherDocumentAdaptor.delete(existing)
        }

        val document =
            teacherDocumentAdaptor.save(
                TeacherDocument(
                    teacher = teacher,
                    file = file,
                    documentType = request.documentType,
                ),
            )
        return TeacherDocumentResponse.from(document)
    }
}
