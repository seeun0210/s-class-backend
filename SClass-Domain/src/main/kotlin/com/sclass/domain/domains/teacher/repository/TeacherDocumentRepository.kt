package com.sclass.domain.domains.teacher.repository

import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherDocumentRepository : JpaRepository<TeacherDocument, String> {
    fun findAllByTeacherId(teacherId: String): List<TeacherDocument>

    fun findByTeacherIdAndDocumentType(
        teacherId: String,
        documentType: TeacherDocumentType,
    ): TeacherDocument?

    fun deleteAllByTeacherId(teacherId: String)
}
