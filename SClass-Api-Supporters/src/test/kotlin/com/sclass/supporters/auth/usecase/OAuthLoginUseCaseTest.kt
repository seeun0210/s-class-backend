package com.sclass.supporters.auth.usecase

import com.sclass.common.jwt.SignupTokenInfo
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
import com.sclass.supporters.auth.dto.OAuthCompleteSignupRequest
import com.sclass.supporters.auth.dto.OAuthLoginRequest
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
                role = Role.STUDENT,
                platform = Platform.SUPPORTERS,
            )
        val userInfo = OAuthUserInfo(id = "oauth-id", email = "test@example.com", name = "테스트")
        val user = mockk<User> { every { id } returns "user-id" }
        val tokenResult = TokenResult(accessToken = "access-token", refreshToken = "refresh-token")

        every { oAuthClientFactory.getClient("GOOGLE") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("oauth-access-token") } returns userInfo
        every { userService.findByOAuthOrNull("oauth-id", AuthProvider.GOOGLE) } returns user
        every { userService.ensureUserRole("user-id", Platform.SUPPORTERS, Role.STUDENT) } just runs
        every { tokenService.issueTokens("user-id", Role.STUDENT) } returns tokenResult

        val result = useCase.login(request)

        assertFalse(result.isNewUser)
        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertNull(result.signupToken)
    }

    @Test
    fun `기존 OAuth 유저 로그인 시 ensureUserRole이 호출된다`() {
        val request =
            OAuthLoginRequest(
                provider = AuthProvider.GOOGLE,
                accessToken = "oauth-access-token",
                role = Role.TEACHER,
                platform = Platform.SUPPORTERS,
            )
        val userInfo = OAuthUserInfo(id = "oauth-id", email = "test@example.com", name = "테스트")
        val user = mockk<User> { every { id } returns "user-id" }
        val tokenResult = TokenResult(accessToken = "at", refreshToken = "rt")

        every { oAuthClientFactory.getClient("GOOGLE") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("oauth-access-token") } returns userInfo
        every { userService.findByOAuthOrNull("oauth-id", AuthProvider.GOOGLE) } returns user
        every { userService.ensureUserRole("user-id", Platform.SUPPORTERS, Role.TEACHER) } just runs
        every { tokenService.issueTokens("user-id", Role.TEACHER) } returns tokenResult

        useCase.login(request)

        verify { userService.ensureUserRole("user-id", Platform.SUPPORTERS, Role.TEACHER) }
    }

    @Test
    fun `oauthId로 못 찾고 email로 기존 유저를 연결하면 토큰을 발급한다`() {
        val request =
            OAuthLoginRequest(
                provider = AuthProvider.KAKAO,
                accessToken = "oauth-access-token",
                role = Role.STUDENT,
                platform = Platform.SUPPORTERS,
            )
        val userInfo = OAuthUserInfo(id = "oauth-id", email = "test@example.com", name = "테스트")
        val user = mockk<User> { every { id } returns "linked-user-id" }
        val tokenResult = TokenResult(accessToken = "at", refreshToken = "rt")

        every { oAuthClientFactory.getClient("KAKAO") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("oauth-access-token") } returns userInfo
        every { userService.findByOAuthOrNull("oauth-id", AuthProvider.KAKAO) } returns null
        every {
            userService.linkOAuthAndEnsureRole(
                email = "test@example.com",
                oauthId = "oauth-id",
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        } returns user
        every { tokenService.issueTokens("linked-user-id", Role.STUDENT) } returns tokenResult

        val result = useCase.login(request)

        assertFalse(result.isNewUser)
        assertEquals("at", result.accessToken)
        assertEquals("rt", result.refreshToken)
    }

    @Test
    fun `oauthId와 email 모두 못 찾으면 signupToken을 발급하고 isNewUser가 true이다`() {
        val request =
            OAuthLoginRequest(
                provider = AuthProvider.GOOGLE,
                accessToken = "oauth-access-token",
                role = Role.STUDENT,
                platform = Platform.SUPPORTERS,
            )
        val userInfo = OAuthUserInfo(id = "oauth-id", email = "new@example.com", name = "신규유저")

        every { oAuthClientFactory.getClient("GOOGLE") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("oauth-access-token") } returns userInfo
        every { userService.findByOAuthOrNull("oauth-id", AuthProvider.GOOGLE) } returns null
        every {
            userService.linkOAuthAndEnsureRole(
                email = "new@example.com",
                oauthId = "oauth-id",
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        } returns null
        every {
            tokenService.issueSignupToken(
                oauthId = "oauth-id",
                provider = AuthProvider.GOOGLE,
                email = "new@example.com",
                name = "신규유저",
                role = Role.STUDENT,
                platform = Platform.SUPPORTERS,
            )
        } returns "signup-token"

        val result = useCase.login(request)

        assertTrue(result.isNewUser)
        assertEquals("signup-token", result.signupToken)
        assertNull(result.accessToken)
    }

    @Test
    fun `지원하지 않는 프로바이더로 로그인하면 예외가 발생한다`() {
        val request =
            OAuthLoginRequest(
                provider = AuthProvider.EMAIL,
                accessToken = "oauth-access-token",
                role = Role.STUDENT,
                platform = Platform.SUPPORTERS,
            )

        every { oAuthClientFactory.getClient("EMAIL") } throws
            IllegalArgumentException("지원하지 않는 OAuth 프로바이더: EMAIL")

        assertThrows<IllegalArgumentException> {
            useCase.login(request)
        }
    }

    @Test
    fun `유효한 signupToken으로 회원가입을 완료하면 토큰을 반환한다`() {
        val request =
            OAuthCompleteSignupRequest(
                signupToken = "encrypted-signup-token",
                phoneNumber = "010-1234-5678",
                profileImageUrl = "https://example.com/profile.jpg",
            )
        val signupInfo =
            SignupTokenInfo(
                oauthId = "oauth-id",
                provider = "GOOGLE",
                email = "test@example.com",
                name = "테스트",
                role = "STUDENT",
                platform = "SUPPORTERS",
            )
        val user = mockk<User> { every { id } returns "new-user-id" }
        val tokenResult = TokenResult(accessToken = "at", refreshToken = "rt")

        every { tokenService.resolveSignupToken("encrypted-signup-token") } returns signupInfo
        every {
            userService.registerWithOAuth(
                oauthId = "oauth-id",
                authProvider = AuthProvider.GOOGLE,
                email = "test@example.com",
                name = "테스트",
                phoneNumber = "010-1234-5678",
                profileImageUrl = "https://example.com/profile.jpg",
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        } returns user
        every { tokenService.issueTokens("new-user-id", Role.STUDENT) } returns tokenResult

        val result = useCase.completeSignup(request)

        assertEquals("at", result.accessToken)
        assertEquals("rt", result.refreshToken)
    }

    @Test
    fun `회원가입 완료 시 registerWithOAuth에 올바른 파라미터가 전달된다`() {
        val request =
            OAuthCompleteSignupRequest(
                signupToken = "encrypted-signup-token",
                phoneNumber = "010-9999-8888",
                profileImageUrl = "https://example.com/img.png",
            )
        val signupInfo =
            SignupTokenInfo(
                oauthId = "oauth-id-2",
                provider = "KAKAO",
                email = "kakao@example.com",
                name = "카카오유저",
                role = "TEACHER",
                platform = "SUPPORTERS",
            )
        val user = mockk<User> { every { id } returns "user-id" }
        val tokenResult = TokenResult(accessToken = "at", refreshToken = "rt")

        every { tokenService.resolveSignupToken("encrypted-signup-token") } returns signupInfo
        every {
            userService.registerWithOAuth(
                oauthId = "oauth-id-2",
                authProvider = AuthProvider.KAKAO,
                email = "kakao@example.com",
                name = "카카오유저",
                phoneNumber = "010-9999-8888",
                profileImageUrl = "https://example.com/img.png",
                platform = Platform.SUPPORTERS,
                role = Role.TEACHER,
            )
        } returns user
        every { tokenService.issueTokens("user-id", Role.TEACHER) } returns tokenResult

        useCase.completeSignup(request)

        verify {
            userService.registerWithOAuth(
                oauthId = "oauth-id-2",
                authProvider = AuthProvider.KAKAO,
                email = "kakao@example.com",
                name = "카카오유저",
                phoneNumber = "010-9999-8888",
                profileImageUrl = "https://example.com/img.png",
                platform = Platform.SUPPORTERS,
                role = Role.TEACHER,
            )
        }
    }

    @Test
    fun `signupToken의 role과 platform이 올바르게 파싱된다`() {
        val request =
            OAuthCompleteSignupRequest(
                signupToken = "encrypted-signup-token",
                phoneNumber = "010-1111-2222",
            )
        val signupInfo =
            SignupTokenInfo(
                oauthId = "oauth-id",
                provider = "GOOGLE",
                email = "test@example.com",
                name = "테스트",
                role = "STUDENT",
                platform = "SUPPORTERS",
            )
        val user = mockk<User> { every { id } returns "user-id" }
        val tokenResult = TokenResult(accessToken = "at", refreshToken = "rt")

        every { tokenService.resolveSignupToken("encrypted-signup-token") } returns signupInfo
        every {
            userService.registerWithOAuth(
                oauthId = any(),
                authProvider = any(),
                email = any(),
                name = any(),
                phoneNumber = any(),
                profileImageUrl = any(),
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        } returns user
        every { tokenService.issueTokens("user-id", Role.STUDENT) } returns tokenResult

        useCase.completeSignup(request)

        verify {
            userService.registerWithOAuth(
                oauthId = "oauth-id",
                authProvider = AuthProvider.GOOGLE,
                email = "test@example.com",
                name = "테스트",
                phoneNumber = "010-1111-2222",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        }
        verify { tokenService.issueTokens("user-id", Role.STUDENT) }
    }

    @Test
    fun `이미 존재하는 이메일로 회원가입하면 UserAlreadyExistsException이 발생한다`() {
        val request =
            OAuthCompleteSignupRequest(
                signupToken = "encrypted-signup-token",
                phoneNumber = "010-1111-2222",
            )
        val signupInfo =
            SignupTokenInfo(
                oauthId = "oauth-id",
                provider = "GOOGLE",
                email = "existing@example.com",
                name = "테스트",
                role = "STUDENT",
                platform = "SUPPORTERS",
            )

        every { tokenService.resolveSignupToken("encrypted-signup-token") } returns signupInfo
        every {
            userService.registerWithOAuth(
                oauthId = "oauth-id",
                authProvider = AuthProvider.GOOGLE,
                email = "existing@example.com",
                name = "테스트",
                phoneNumber = "010-1111-2222",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        } throws UserAlreadyExistsException()

        assertThrows<UserAlreadyExistsException> {
            useCase.completeSignup(request)
        }
    }
}
