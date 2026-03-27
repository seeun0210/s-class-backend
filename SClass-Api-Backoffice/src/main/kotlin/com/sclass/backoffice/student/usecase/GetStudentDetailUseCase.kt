package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.StudentDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetStudentDetailUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(studentId: String): StudentDetailResponse {
        val student = studentAdaptor.findById(studentId)
        val roles = userRoleAdaptor.findAllByUserId(student.user.id)
        return StudentDetailResponse.from(student, roles)
    }
}
