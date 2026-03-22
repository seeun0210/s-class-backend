package com.sclass.domain.domains.teacher.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import com.sclass.domain.domains.teacher.exception.TeacherDocumentNotFoundException
import com.sclass.domain.domains.teacher.repository.TeacherDocumentRepository

@Adaptor
class TeacherDocumentAdaptor(
    private val teacherDocumentRepository: TeacherDocumentRepository,
) {
    fun findById(id: String): TeacherDocument = teacherDocumentRepository.findById(id).orElseThrow { TeacherDocumentNotFoundException() }

    fun findByIdOrNull(id: String): TeacherDocument? = teacherDocumentRepository.findById(id).orElse(null)

    fun findAllByTeacherId(teacherId: String): List<TeacherDocument> = teacherDocumentRepository.findAllByTeacherId(teacherId)

    fun findByTeacherIdAndDocumentType(
        teacherId: String,
        documentType: TeacherDocumentType,
    ): TeacherDocument? = teacherDocumentRepository.findByTeacherIdAndDocumentType(teacherId, documentType)

    fun save(teacherDocument: TeacherDocument): TeacherDocument = teacherDocumentRepository.save(teacherDocument)

    fun deleteAllByTeacherId(teacherId: String) = teacherDocumentRepository.deleteAllByTeacherId(teacherId)
}
