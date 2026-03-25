package com.sclass.supporters.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.TeacherProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class SubmitTeacherVerificationUseCase(
    private val teacherDomainService: TeacherDomainService,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
) {
    @Transactional
    fun execute(userId: String): TeacherProfileResponse {
        val teacher = teacherDomainService.findByUserId(userId)
        val submitted = teacherDomainService.submitForVerification(teacher)
        val documents = teacherDocumentAdaptor.findAllByTeacherId(submitted.id)
        return TeacherProfileResponse.from(
            teacher = submitted,
            documents = documents.map { TeacherDocumentResponse.from(it) },
        )
    }
}
