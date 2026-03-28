package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.TeacherDocumentResponse
import com.sclass.backoffice.teacher.dto.UploadTeacherDocumentRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import org.springframework.transaction.annotation.Transactional

@UseCase
class UploadTeacherDocumentUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
    private val fileAdaptor: FileAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: UploadTeacherDocumentRequest,
    ): TeacherDocumentResponse {
        val teacher = teacherAdaptor.findByUserId(userId)
        val file = fileAdaptor.findById(request.fileId)

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
