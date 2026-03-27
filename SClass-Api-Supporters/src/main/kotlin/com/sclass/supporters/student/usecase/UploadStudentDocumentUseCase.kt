package com.sclass.supporters.student.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.supporters.student.dto.StudentDocumentResponse
import com.sclass.supporters.student.dto.UploadStudentDocumentRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UploadStudentDocumentUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val studentDocumentAdaptor: StudentDocumentAdaptor,
    private val fileAdaptor: FileAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: UploadStudentDocumentRequest,
    ): StudentDocumentResponse {
        val student = studentAdaptor.findByUserId(userId)
        val file = fileAdaptor.findById(request.fileId)

        val existing = studentDocumentAdaptor.findByStudentIdAndDocumentType(student.id, request.documentType)
        if (existing != null) {
            studentDocumentAdaptor.delete(existing)
        }

        val document =
            studentDocumentAdaptor.save(
                StudentDocument(
                    student = student,
                    file = file,
                    documentType = request.documentType,
                ),
            )
        return StudentDocumentResponse.from(document)
    }
}
