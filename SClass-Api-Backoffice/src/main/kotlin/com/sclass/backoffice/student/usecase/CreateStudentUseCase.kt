package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.CreateStudentRequest
import com.sclass.backoffice.student.dto.CreateStudentResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateStudentUseCase(
    private val userDomainService: UserDomainService,
    private val studentDomainService: StudentDomainService,
) {
    @Transactional
    fun execute(request: CreateStudentRequest): CreateStudentResponse {
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
                role = Role.STUDENT,
            )

        var student = studentDomainService.register(savedUser)

        if (request.grade != null && request.school != null) {
            student =
                studentDomainService.updateProfile(
                    student = student,
                    grade = request.grade,
                    school = request.school,
                    parentPhoneNumber = request.parentPhoneNumber,
                )
        }

        return CreateStudentResponse(
            studentId = student.id,
            userId = savedUser.id,
            email = savedUser.email,
            name = savedUser.name,
            platform = request.platform,
            phoneNumber = formattedPhone,
            grade = student.grade,
            school = student.school,
            parentPhoneNumber = student.parentPhoneNumber,
        )
    }

    companion object {
        private const val DEFAULT_PASSWORD = "12345678"
    }
}
