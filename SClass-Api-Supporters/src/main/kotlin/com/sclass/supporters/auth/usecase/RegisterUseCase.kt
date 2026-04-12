package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.exception.VerificationTokenMismatchException
import com.sclass.supporters.auth.dto.RegisterRequest
import com.sclass.supporters.auth.dto.TokenResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class RegisterUseCase(
    private val userService: UserDomainService,
    private val tokenService: TokenDomainService,
    private val teacherDomainService: TeacherDomainService,
    private val studentDomainService: StudentDomainService,
) {
    @Transactional
    fun execute(request: RegisterRequest): TokenResponse {
        val phoneNumber = User.formatPhoneNumber(request.phoneNumber)
        verifyToken(request.phoneVerificationToken, VerificationChannel.PHONE, phoneNumber)
        verifyToken(request.emailVerificationToken, VerificationChannel.EMAIL, request.email)

        val user =
            userService.register(
                user =
                    User(
                        email = request.email,
                        name = request.name,
                        authProvider = AuthProvider.EMAIL,
                        phoneNumber = phoneNumber,
                    ),
                rawPassword = request.password,
                platform = Platform.SUPPORTERS,
                role = request.role,
            )

        createRoleProfile(user, request.role)

        val tokens = tokenService.issueTokens(user.id, request.role)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    private fun createRoleProfile(
        user: User,
        role: Role,
    ) {
        when (role) {
            Role.TEACHER -> teacherDomainService.register(user)
            Role.STUDENT -> studentDomainService.register(user)
            else -> {}
        }
    }

    private fun verifyToken(
        encryptedToken: String,
        expectedChannel: VerificationChannel,
        expectedTarget: String,
    ) {
        val tokenInfo = tokenService.resolveVerificationToken(encryptedToken)
        if (tokenInfo.channel != expectedChannel.name || tokenInfo.target != expectedTarget) {
            throw VerificationTokenMismatchException()
        }
    }
}
