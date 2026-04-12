package com.sclass.supporters.auth.usecase

import com.sclass.common.jwt.VerificationTokenInfo
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.domain.domains.verification.exception.VerificationTokenMismatchException
import com.sclass.supporters.auth.dto.RegisterRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RegisterUseCaseTest {
    private lateinit var userService: UserDomainService
    private lateinit var tokenService: TokenDomainService
    private lateinit var teacherDomainService: TeacherDomainService
    private lateinit var studentDomainService: StudentDomainService
    private lateinit var useCase: RegisterUseCase

    @BeforeEach
    fun setUp() {
        userService = mockk()
        tokenService = mockk()
        teacherDomainService = mockk(relaxed = true)
        studentDomainService = mockk(relaxed = true)
        useCase = RegisterUseCase(userService, tokenService, teacherDomainService, studentDomainService)
    }

    private fun createRequest(
        phoneVerificationToken: String = "phone-token",
        emailVerificationToken: String = "email-token",
    ) = RegisterRequest(
        email = "test@example.com",
        password = "password123",
        name = "홍길동",
        phoneNumber = "01012345678",
        role = Role.STUDENT,
        phoneVerificationToken = phoneVerificationToken,
        emailVerificationToken = emailVerificationToken,
    )

    private fun mockValidTokens() {
        every { tokenService.resolveVerificationToken("phone-token") } returns
            VerificationTokenInfo(channel = "PHONE", target = "010-1234-5678")
        every { tokenService.resolveVerificationToken("email-token") } returns
            VerificationTokenInfo(channel = "EMAIL", target = "test@example.com")
    }

    @Test
    fun `유효한 인증 토큰으로 회원가입하면 accessToken과 refreshToken을 반환한다`() {
        mockValidTokens()

        val user = User(email = "test@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL, phoneNumber = "010-1234-5678")
        every { userService.register(any(), any(), Platform.SUPPORTERS, Role.STUDENT) } returns user
        every { tokenService.issueTokens(user.id, Role.STUDENT) } returns
            TokenResult(accessToken = "access", refreshToken = "refresh")

        val result = useCase.execute(createRequest())

        assertEquals("access", result.accessToken)
        assertEquals("refresh", result.refreshToken)
        verify { userService.register(any(), "password123", Platform.SUPPORTERS, Role.STUDENT) }
    }

    @Test
    fun `휴대전화 인증 토큰의 채널이 불일치하면 VerificationTokenMismatchException이 발생한다`() {
        every { tokenService.resolveVerificationToken("phone-token") } returns
            VerificationTokenInfo(channel = "EMAIL", target = "010-1234-5678")

        assertThrows<VerificationTokenMismatchException> {
            useCase.execute(createRequest())
        }
    }

    @Test
    fun `휴대전화 인증 토큰의 대상이 불일치하면 VerificationTokenMismatchException이 발생한다`() {
        every { tokenService.resolveVerificationToken("phone-token") } returns
            VerificationTokenInfo(channel = "PHONE", target = "010-9999-9999")

        assertThrows<VerificationTokenMismatchException> {
            useCase.execute(createRequest())
        }
    }

    @Test
    fun `이메일 인증 토큰의 채널이 불일치하면 VerificationTokenMismatchException이 발생한다`() {
        every { tokenService.resolveVerificationToken("phone-token") } returns
            VerificationTokenInfo(channel = "PHONE", target = "010-1234-5678")
        every { tokenService.resolveVerificationToken("email-token") } returns
            VerificationTokenInfo(channel = "PHONE", target = "test@example.com")

        assertThrows<VerificationTokenMismatchException> {
            useCase.execute(createRequest())
        }
    }

    @Test
    fun `이메일 인증 토큰의 대상이 불일치하면 VerificationTokenMismatchException이 발생한다`() {
        every { tokenService.resolveVerificationToken("phone-token") } returns
            VerificationTokenInfo(channel = "PHONE", target = "010-1234-5678")
        every { tokenService.resolveVerificationToken("email-token") } returns
            VerificationTokenInfo(channel = "EMAIL", target = "other@example.com")

        assertThrows<VerificationTokenMismatchException> {
            useCase.execute(createRequest())
        }
    }
}
