package com.sclass.domain.domains.student.repository

import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
import org.springframework.data.jpa.repository.JpaRepository

interface StudentDocumentRepository : JpaRepository<StudentDocument, String> {
    fun findAllByStudentId(studentId: String): List<StudentDocument>

    fun findByStudentIdAndDocumentType(
        studentId: String,
        documentType: StudentDocumentType,
    ): StudentDocument?

    fun findAllByStudentIdAndDocumentType(
        studentId: String,
        documentType: StudentDocumentType,
    ): List<StudentDocument>

    fun deleteAllByStudentId(studentId: String)
}
