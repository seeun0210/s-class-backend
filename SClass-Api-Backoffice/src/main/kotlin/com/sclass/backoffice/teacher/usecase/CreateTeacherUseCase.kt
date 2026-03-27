package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.CreateTeacherRequest
import com.sclass.backoffice.teacher.dto.CreateTeacherResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateTeacherUseCase(
    private val userDomainService: UserDomainService,
    private val teacherDomainService: TeacherDomainService,
) {
    @Transactional
    fun execute(request: CreateTeacherRequest): CreateTeacherResponse {
        val user =
            User(
                email = request.email,
                name = request.name,
                authProvider = AuthProvider.EMAIL,
            )

        val savedUser =
            userDomainService.register(
                user = user,
                rawPassword = DEFAULT_PASSWORD,
                platform = request.platform,
                role = Role.TEACHER,
            )

        val teacher = teacherDomainService.register(savedUser)

        return CreateTeacherResponse(
            teacherId = teacher.id,
            userId = savedUser.id,
            email = savedUser.email,
            name = savedUser.name,
            platform = request.platform,
        )
    }

    companion object {
        private const val DEFAULT_PASSWORD = "12345678"
    }
}
