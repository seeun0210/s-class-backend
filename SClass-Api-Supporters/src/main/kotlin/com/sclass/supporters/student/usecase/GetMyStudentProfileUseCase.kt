package com.sclass.supporters.student.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.activePlatforms
import com.sclass.supporters.student.dto.StudentDocumentResponse
import com.sclass.supporters.student.dto.StudentProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyStudentProfileUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val studentDocumentAdaptor: StudentDocumentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): StudentProfileResponse {
        val student = studentAdaptor.findByUserId(userId)
        val documents = studentDocumentAdaptor.findAllByStudentId(student.id)
        val platforms = userRoleAdaptor.findAllByUserId(userId).activePlatforms()
        return StudentProfileResponse.from(
            student = student,
            platforms = platforms,
            documents = documents.map { StudentDocumentResponse.from(it) },
        )
    }
}
