package com.sclass.supporters.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.TeacherProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyTeacherProfileUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): TeacherProfileResponse {
        val teacher = teacherAdaptor.findByUserId(userId)
        val documents = teacherDocumentAdaptor.findAllByTeacherId(teacher.id)
        return TeacherProfileResponse.from(
            teacher = teacher,
            documents = documents.map { TeacherDocumentResponse.from(it) },
        )
    }
}
