package com.sclass.domain.domains.student.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.student.exception.StudentDocumentNotFoundException
import com.sclass.domain.domains.student.repository.StudentDocumentRepository

@Adaptor
class StudentDocumentAdaptor(
    private val studentDocumentRepository: StudentDocumentRepository,
) {
    fun findById(id: String): StudentDocument = studentDocumentRepository.findById(id).orElseThrow { StudentDocumentNotFoundException() }

    fun findByIdOrNull(id: String): StudentDocument? = studentDocumentRepository.findById(id).orElse(null)

    fun findAllByStudentId(studentId: String): List<StudentDocument> = studentDocumentRepository.findAllByStudentId(studentId)

    fun findByStudentIdAndDocumentType(
        studentId: String,
        documentType: StudentDocumentType,
    ): StudentDocument? = studentDocumentRepository.findByStudentIdAndDocumentType(studentId, documentType)

    fun findAllByStudentIdAndDocumentType(
        studentId: String,
        documentType: StudentDocumentType,
    ): List<StudentDocument> = studentDocumentRepository.findAllByStudentIdAndDocumentType(studentId, documentType)

    fun save(studentDocument: StudentDocument): StudentDocument = studentDocumentRepository.save(studentDocument)

    fun delete(studentDocument: StudentDocument) = studentDocumentRepository.delete(studentDocument)

    fun deleteAllByStudentId(studentId: String) = studentDocumentRepository.deleteAllByStudentId(studentId)
}
