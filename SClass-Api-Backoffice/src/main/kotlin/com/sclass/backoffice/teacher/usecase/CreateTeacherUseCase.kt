package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.CreateTeacherRequest
import com.sclass.backoffice.teacher.dto.CreateTeacherResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.domain.TeacherEducation
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
        val formattedPhone = User.formatPhoneNumber(request.phoneNumber)
        val user =
            User(
                email = request.email,
                name = request.name,
                authProvider = AuthProvider.EMAIL,
                phoneNumber = formattedPhone,
            )

        val savedUser =
            userDomainService.register(
                user = user,
                rawPassword = DEFAULT_PASSWORD,
                platform = request.platform,
                role = Role.TEACHER,
            )

        val education =
            TeacherEducation(
                university = request.university,
                major = request.major,
                majorCategory = request.majorCategory,
            )

        val teacher = teacherDomainService.register(savedUser, education)

        return CreateTeacherResponse(
            teacherId = teacher.id,
            userId = savedUser.id,
            email = savedUser.email,
            name = savedUser.name,
            platform = request.platform,
            phoneNumber = formattedPhone,
            university = teacher.education?.university,
            major = teacher.education?.major,
            majorCategory = teacher.education?.majorCategory,
        )
    }

    companion object {
        private const val DEFAULT_PASSWORD = "12345678"
    }
}
