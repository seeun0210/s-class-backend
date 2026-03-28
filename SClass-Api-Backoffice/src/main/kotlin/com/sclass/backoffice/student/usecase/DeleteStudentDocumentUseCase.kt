package com.sclass.backoffice.student.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.infrastructure.s3.S3Service
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteStudentDocumentUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val studentDocumentAdaptor: StudentDocumentAdaptor,
    private val fileAdaptor: FileAdaptor,
    private val s3Service: S3Service,
) {
    @Transactional
    fun execute(
        userId: String,
        documentId: String,
    ) {
        val student = studentAdaptor.findByUserId(userId)
        val document = studentDocumentAdaptor.findById(documentId)

        require(document.student.id == student.id) { "해당 학생의 문서가 아닙니다." }

        studentDocumentAdaptor.delete(document)
        fileAdaptor.delete(document.file.id)
        s3Service.deleteObject(document.file.storedFilename)
    }
}
