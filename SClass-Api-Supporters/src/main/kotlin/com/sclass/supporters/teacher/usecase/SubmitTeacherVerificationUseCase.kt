package com.sclass.supporters.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.activePlatforms
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.TeacherProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class SubmitTeacherVerificationUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDomainService: TeacherDomainService,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional
    fun execute(userId: String): TeacherProfileResponse {
        val teacher = teacherAdaptor.findByUserId(userId)
        val submitted = teacherDomainService.submitForVerification(teacher, Platform.SUPPORTERS)
        val documents = teacherDocumentAdaptor.findAllByTeacherId(submitted.id)
        val allRoles = userRoleAdaptor.findAllByUserId(userId)
        val userRole =
            allRoles.find { it.platform == Platform.SUPPORTERS && it.role == Role.TEACHER }
                ?: throw RoleNotFoundException()
        val platforms = allRoles.activePlatforms()
        return TeacherProfileResponse.from(
            teacher = submitted,
            userRole = userRole,
            platforms = platforms,
            documents = documents.map { TeacherDocumentResponse.from(it) },
        )
    }
}
