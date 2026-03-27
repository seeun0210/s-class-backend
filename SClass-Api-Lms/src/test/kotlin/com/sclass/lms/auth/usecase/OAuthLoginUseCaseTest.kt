package com.sclass.lms.auth.usecase

import com.sclass.common.jwt.SignupTokenInfo
import com.sclass.common.jwt.VerificationTokenInfo
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.exception.UserAlreadyExistsException
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.infrastructure.oauth.OAuthClientFactory
import com.sclass.infrastructure.oauth.client.OAuthClient
import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import com.sclass.lms.auth.dto.OAuthCompleteSignupRequest
import com.sclass.lms.auth.dto.OAuthLoginRequest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OAuthLoginUseCaseTest {
    private lateinit var oAuthClientFactory: OAuthClientFactory
    private lateinit var userService: UserDomainService
    private lateinit var tokenService: TokenDomainService
    private lateinit var oAuthClient: OAuthClient
    private lateinit var useCase: OAuthLoginUseCase

    @BeforeEach
    fun setUp() {
        oAuthClientFactory = mockk()
        userService = mockk()
        tokenService = mockk()
        oAuthClient = mockk()
        useCase = OAuthLoginUseCase(oAuthClientFactory, userService, tokenService)
    }

    @Test
    fun `oauthId로 기존 유저를 찾으면 토큰을 발급하고 isNewUser가 false이다`() {
        val request =
            OAuthLoginRequest(
                provider = AuthProvider.GOOGLE,
                accessToken = "oauth-access-token",
                role = Role.ADMIN,
                platform = Platform.LMS,
            )
        val userInfo = OAuthUserInfo(id = "oauth-id", email = "admin@example.com", name = "관리자")
        val user = mockk<User> { every { id } returns "user-id" }
        val tokenResult = TokenResult(accessToken = "access-token", refreshToken = "refresh-token")

        every { oAuthClientFactory.getClient("GOOGLE") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("oauth-access-token") } returns userInfo
        every { userService.findByOAuthOrNull("oauth-id", AuthProvider.GOOGLE) } returns user
        every { userService.ensureUserRole("user-id", Platform.LMS, Role.ADMIN) } just runs
        every { userService.activateIfApproved("user-id", Platform.LMS, Role.ADMIN) } just Runs
        every { tokenService.issueTokens("user-id", Role.ADMIN, Platform.LMS) } returns tokenResult

        val result = useCase.login(request)

        assertFalse(result.isNewUser)
        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertNull(result.signupToken)
    }

    @Test
    fun `신규 유저는 signupToken을 발급받고 isNewUser가 true이다`() {
        val request =
            OAuthLoginRequest(
                provider = AuthProvider.GOOGLE,
                accessToken = "oauth-access-token",
                role = Role.TEACHER,
                platform = Platform.LMS,
            )
        val userInfo = OAuthUserInfo(id = "oauth-id", email = "new@example.com", name = "신규")

        every { oAuthClientFactory.getClient("GOOGLE") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("oauth-access-token") } returns userInfo
        every { userService.findByOAuthOrNull("oauth-id", AuthProvider.GOOGLE) } returns null
        every {
            userService.linkOAuthAndEnsureRole(
                email = "new@example.com",
                oauthId = "oauth-id",
                platform = Platform.LMS,
                role = Role.TEACHER,
            )
        } returns null
        every {
            tokenService.issueSignupToken(
                oauthId = "oauth-id",
                provider = AuthProvider.GOOGLE,
                email = "new@example.com",
                name = "신규",
                role = Role.TEACHER,
                platform = Platform.LMS,
            )
        } returns "signup-token"

        val result = useCase.login(request)

        assertTrue(result.isNewUser)
        assertEquals("signup-token", result.signupToken)
        assertNull(result.accessToken)
    }

    @Test
    fun `회원가입 완료 시 profileImageUrl이 올바르게 전달된다`() {
        val request =
            OAuthCompleteSignupRequest(
                signupToken = "encrypted-signup-token",
                phoneVerificationToken = "encrypted-phone-token",
                profileImageUrl = "https://example.com/profile.jpg",
            )
        val signupInfo =
            SignupTokenInfo(
                oauthId = "oauth-id",
                provider = "GOOGLE",
                email = "test@example.com",
                name = "테스트",
                role = "ADMIN",
                platform = "LMS",
            )
        val phoneInfo = VerificationTokenInfo(channel = "PHONE", target = "010-1234-5678")
        val user = mockk<User> { every { id } returns "user-id" }
        val tokenResult = TokenResult(accessToken = "at", refreshToken = "rt")

        every { tokenService.resolveSignupToken("encrypted-signup-token") } returns signupInfo
        every { tokenService.resolveVerificationToken("encrypted-phone-token") } returns phoneInfo
        every {
            userService.registerWithOAuth(
                oauthId = "oauth-id",
                authProvider = AuthProvider.GOOGLE,
                email = "test@example.com",
                name = "테스트",
                phoneNumber = "010-1234-5678",
                profileImageUrl = "https://example.com/profile.jpg",
                platform = Platform.LMS,
                role = Role.ADMIN,
            )
        } returns user
        every { tokenService.issueTokens("user-id", Role.ADMIN, Platform.LMS) } returns tokenResult

        useCase.completeSignup(request)

        verify {
            userService.registerWithOAuth(
                oauthId = "oauth-id",
                authProvider = AuthProvider.GOOGLE,
                email = "test@example.com",
                name = "테스트",
                phoneNumber = "010-1234-5678",
                profileImageUrl = "https://example.com/profile.jpg",
                platform = Platform.LMS,
                role = Role.ADMIN,
            )
        }
    }

    @Test
    fun `유효한 signupToken으로 회원가입을 완료하면 토큰을 반환한다`() {
        val request =
            OAuthCompleteSignupRequest(
                signupToken = "encrypted-signup-token",
                phoneVerificationToken = "encrypted-phone-token",
            )
        val signupInfo =
            SignupTokenInfo(
                oauthId = "oauth-id",
                provider = "KAKAO",
                email = "kakao@example.com",
                name = "카카오유저",
                role = "TEACHER",
                platform = "LMS",
            )
        val phoneInfo = VerificationTokenInfo(channel = "PHONE", target = "010-5555-6666")
        val user = mockk<User> { every { id } returns "new-user-id" }
        val tokenResult = TokenResult(accessToken = "access-token", refreshToken = "refresh-token")

        every { tokenService.resolveSignupToken("encrypted-signup-token") } returns signupInfo
        every { tokenService.resolveVerificationToken("encrypted-phone-token") } returns phoneInfo
        every {
            userService.registerWithOAuth(
                oauthId = "oauth-id",
                authProvider = AuthProvider.KAKAO,
                email = "kakao@example.com",
                name = "카카오유저",
                phoneNumber = "010-5555-6666",
                profileImageUrl = null,
                platform = Platform.LMS,
                role = Role.TEACHER,
            )
        } returns user
        every { tokenService.issueTokens("new-user-id", Role.TEACHER, Platform.LMS) } returns tokenResult

        val result = useCase.completeSignup(request)

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
    }

    @Test
    fun `이미 존재하는 이메일로 회원가입하면 UserAlreadyExistsException이 발생한다`() {
        val request =
            OAuthCompleteSignupRequest(
                signupToken = "encrypted-signup-token",
                phoneVerificationToken = "encrypted-phone-token",
            )
        val signupInfo =
            SignupTokenInfo(
                oauthId = "oauth-id",
                provider = "GOOGLE",
                email = "existing@example.com",
                name = "테스트",
                role = "ADMIN",
                platform = "LMS",
            )
        val phoneInfo = VerificationTokenInfo(channel = "PHONE", target = "010-1234-5678")

        every { tokenService.resolveSignupToken("encrypted-signup-token") } returns signupInfo
        every { tokenService.resolveVerificationToken("encrypted-phone-token") } returns phoneInfo
        every {
            userService.registerWithOAuth(
                oauthId = "oauth-id",
                authProvider = AuthProvider.GOOGLE,
                email = "existing@example.com",
                name = "테스트",
                phoneNumber = "010-1234-5678",
                profileImageUrl = null,
                platform = Platform.LMS,
                role = Role.ADMIN,
            )
        } throws UserAlreadyExistsException()

        assertThrows<UserAlreadyExistsException> {
            useCase.completeSignup(request)
        }
    }
}
