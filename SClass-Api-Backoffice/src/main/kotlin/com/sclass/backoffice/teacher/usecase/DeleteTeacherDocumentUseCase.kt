package com.sclass.backoffice.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.infrastructure.s3.S3Service
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteTeacherDocumentUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
    private val fileAdaptor: FileAdaptor,
    private val s3Service: S3Service,
) {
    @Transactional
    fun execute(
        userId: String,
        documentId: String,
    ) {
        val teacher = teacherAdaptor.findByUserId(userId)
        val document = teacherDocumentAdaptor.findById(documentId)

        require(document.teacher.id == teacher.id) { "해당 선생님의 문서가 아닙니다." }

        teacherDocumentAdaptor.delete(document)
        fileAdaptor.delete(document.file.id)
        s3Service.deleteObject(document.file.storedFilename)
    }
}
