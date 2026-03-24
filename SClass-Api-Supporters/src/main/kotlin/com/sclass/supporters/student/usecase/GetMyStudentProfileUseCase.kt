package com.sclass.supporters.student.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.supporters.student.dto.StudentDocumentResponse
import com.sclass.supporters.student.dto.StudentProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyStudentProfileUseCase(
    private val studentDomainService: StudentDomainService,
    private val studentDocumentAdaptor: StudentDocumentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): StudentProfileResponse {
        val student = studentDomainService.findByUserId(userId)
        val documents = studentDocumentAdaptor.findAllByStudentId(student.id)
        return StudentProfileResponse.from(
            student = student,
            documents = documents.map { StudentDocumentResponse.from(it) },
        )
    }
}
