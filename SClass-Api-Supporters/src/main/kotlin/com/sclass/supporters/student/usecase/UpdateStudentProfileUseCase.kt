package com.sclass.supporters.student.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.supporters.student.dto.StudentDocumentResponse
import com.sclass.supporters.student.dto.StudentProfileResponse
import com.sclass.supporters.student.dto.UpdateStudentProfileRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateStudentProfileUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val studentDomainService: StudentDomainService,
    private val studentDocumentAdaptor: StudentDocumentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
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
        val platforms =
            userRoleAdaptor
                .findAllByUserId(userId)
                .filter { it.state.isActive }
                .map { it.platform }
                .distinct()
        return StudentProfileResponse.from(
            student = updated,
            platforms = platforms,
            documents = documents.map { StudentDocumentResponse.from(it) },
        )
    }
}
