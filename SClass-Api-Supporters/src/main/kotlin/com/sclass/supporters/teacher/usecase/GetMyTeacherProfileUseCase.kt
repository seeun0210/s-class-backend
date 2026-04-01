package com.sclass.supporters.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.TeacherProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyTeacherProfileUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): TeacherProfileResponse {
        val teacher = teacherAdaptor.findByUserId(userId)
        val documents = teacherDocumentAdaptor.findAllByTeacherId(teacher.id)
        val userRole =
            userRoleAdaptor.findByUserIdAndPlatformAndRole(userId, Platform.SUPPORTERS, Role.TEACHER)
                ?: throw RoleNotFoundException()
        return TeacherProfileResponse.from(
            teacher = teacher,
            userRole = userRole,
            documents = documents.map { TeacherDocumentResponse.from(it) },
        )
    }
}
