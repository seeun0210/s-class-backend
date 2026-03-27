package com.sclass.supporters.student.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.supporters.student.dto.StudentDocumentResponse
import com.sclass.supporters.student.dto.StudentProfileResponse
import com.sclass.supporters.student.dto.UpdateStudentProfileRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateStudentProfileUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val studentDomainService: StudentDomainService,
    private val studentDocumentAdaptor: StudentDocumentAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: UpdateStudentProfileRequest,
    ): StudentProfileResponse {
        val student = studentAdaptor.findByUserId(userId)
        val updated =
            studentDomainService.updateProfile(
                student = student,
                grade = request.grade,
                school = request.school,
                parentPhoneNumber = request.parentPhoneNumber,
            )
        val documents = studentDocumentAdaptor.findAllByStudentId(updated.id)
        return StudentProfileResponse.from(
            student = updated,
            documents = documents.map { StudentDocumentResponse.from(it) },
        )
    }
}
