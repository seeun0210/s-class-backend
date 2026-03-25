package com.sclass.supporters.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.TeacherProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyTeacherProfileUseCase(
    private val teacherDomainService: TeacherDomainService,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): TeacherProfileResponse {
        val teacher = teacherDomainService.findByUserId(userId)
        val documents = teacherDocumentAdaptor.findAllByTeacherId(teacher.id)
        return TeacherProfileResponse.from(
            teacher = teacher,
            documents = documents.map { TeacherDocumentResponse.from(it) },
        )
    }
}
