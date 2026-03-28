package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.UpdateStudentStateRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateStudentStateUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: UpdateStudentStateRequest,
    ) {
        val student = studentAdaptor.findByUserId(userId)
        val userRole =
            userRoleAdaptor.findByUserIdAndPlatformAndRole(
                userId = userId,
                platform = request.platform,
                role = Role.STUDENT,
            ) ?: throw RoleNotFoundException()
        userRole.state = request.state
    }
}
