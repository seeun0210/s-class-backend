package com.sclass.domain.domains.user.service

import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.exception.ConflictingRoleException
import com.sclass.domain.domains.user.exception.InvalidPasswordException
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import com.sclass.domain.domains.user.exception.UserAlreadyExistsException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserDomainServiceTest {
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var passwordService: PasswordService
    private lateinit var userDomainService: UserDomainService

    @BeforeEach
    fun setUp() {
        userAdaptor = mockk()
        userRoleAdaptor = mockk()
        passwordService = mockk()
        userDomainService = UserDomainService(userAdaptor, userRoleAdaptor, passwordService)
    }

    @Nested
    inner class Register {
        @Test
        fun `성공적으로 회원가입하면 비밀번호 해싱 후 유저와 역할이 저장된다`() {
            val user =
                User(
                    email = "test@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.EMAIL,
                )
            every { userAdaptor.existsByEmail("test@example.com") } returns false
            every { passwordService.hash("rawPassword") } returns "hashedPassword"
            every { userAdaptor.save(user) } returns user
            every { userRoleAdaptor.save(any()) } returns mockk()

            val result = userDomainService.register(user, "rawPassword", Platform.SUPPORTERS, Role.STUDENT)

            assertEquals("hashedPassword", result.hashedPassword)
            verify { userAdaptor.save(user) }
            verify { userRoleAdaptor.save(any()) }
        }

        @Test
        fun `이미 존재하는 이메일로 회원가입하면 UserAlreadyExistsException이 발생한다`() {
            val user =
                User(
                    email = "existing@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.EMAIL,
                )
            every { userAdaptor.existsByEmail("existing@example.com") } returns true

            assertThrows<UserAlreadyExistsException> {
                userDomainService.register(user, "rawPassword", Platform.SUPPORTERS, Role.STUDENT)
            }
        }

        @Test
        fun `회원가입 시 UserRole에 올바른 userId가 설정된다`() {
            val user =
                User(
                    email = "test@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.EMAIL,
                )
            val userRoleSlot = slot<UserRole>()

            every { userAdaptor.existsByEmail("test@example.com") } returns false
            every { passwordService.hash("rawPassword") } returns "hashed"
            every { userAdaptor.save(user) } returns user
            every { userRoleAdaptor.save(capture(userRoleSlot)) } returns mockk()

            userDomainService.register(user, "rawPassword", Platform.LMS, Role.ADMIN)

            val savedUserRole = userRoleSlot.captured
            assertEquals(user.id, savedUserRole.userId)
            assertEquals(Platform.LMS, savedUserRole.platform)
            assertEquals(Role.ADMIN, savedUserRole.role)
        }
    }

    @Nested
    inner class Authenticate {
        @Test
        fun `올바른 이메일과 비밀번호로 인증하면 유저를 반환한다`() {
            val user =
                User(
                    email = "test@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashedPw",
                )
            every { userAdaptor.findByEmail("test@example.com") } returns user
            every { passwordService.matches("rawPw", "hashedPw") } returns true
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.STUDENT)
            } returns mockk()

            val result = userDomainService.authenticate("test@example.com", "rawPw", Platform.SUPPORTERS, Role.STUDENT)

            assertEquals(user, result)
        }

        @Test
        fun `비밀번호가 null이면 InvalidPasswordException이 발생한다`() {
            val user =
                User(
                    email = "test@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.GOOGLE,
                    hashedPassword = null,
                )
            every { userAdaptor.findByEmail("test@example.com") } returns user

            assertThrows<InvalidPasswordException> {
                userDomainService.authenticate("test@example.com", "rawPw", Platform.SUPPORTERS, Role.STUDENT)
            }
        }

        @Test
        fun `비밀번호가 일치하지 않으면 InvalidPasswordException이 발생한다`() {
            val user =
                User(
                    email = "test@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashedPw",
                )
            every { userAdaptor.findByEmail("test@example.com") } returns user
            every { passwordService.matches("wrongPw", "hashedPw") } returns false

            assertThrows<InvalidPasswordException> {
                userDomainService.authenticate("test@example.com", "wrongPw", Platform.SUPPORTERS, Role.STUDENT)
            }
        }

        @Test
        fun `해당 role이 없으면 RoleNotFoundException이 발생한다`() {
            val user =
                User(
                    email = "test@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashedPw",
                )
            every { userAdaptor.findByEmail("test@example.com") } returns user
            every { passwordService.matches("rawPw", "hashedPw") } returns true
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.LMS, Role.ADMIN)
            } returns null

            assertThrows<RoleNotFoundException> {
                userDomainService.authenticate("test@example.com", "rawPw", Platform.LMS, Role.ADMIN)
            }
        }
    }

    @Nested
    inner class FindByOAuthOrNull {
        @Test
        fun `oauthId와 provider로 유저를 찾으면 반환한다`() {
            val user = mockk<User>()
            every { userAdaptor.findByOauthId("oauth-id", AuthProvider.GOOGLE) } returns user

            val result = userDomainService.findByOAuthOrNull("oauth-id", AuthProvider.GOOGLE)

            assertEquals(user, result)
        }

        @Test
        fun `oauthId로 유저를 찾지 못하면 null을 반환한다`() {
            every { userAdaptor.findByOauthId("unknown-id", AuthProvider.GOOGLE) } returns null

            val result = userDomainService.findByOAuthOrNull("unknown-id", AuthProvider.GOOGLE)

            assertNull(result)
        }
    }

    @Nested
    inner class LinkOAuthAndEnsureRole {
        @Test
        fun `이메일로 기존 유저를 찾으면 oauthId를 설정하고 저장한다`() {
            val user =
                User(
                    email = "test@example.com",
                    name = "테스트",
                    authProvider = AuthProvider.EMAIL,
                )
            every { userAdaptor.findByEmailOrNull("test@example.com") } returns user
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.STUDENT)
            } returns null
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns null
            every { userRoleAdaptor.save(any()) } returns mockk()
            every { userAdaptor.save(user) } returns user

            val result =
                userDomainService.linkOAuthAndEnsureRole(
                    email = "test@example.com",
                    oauthId = "new-oauth-id",
                    platform = Platform.SUPPORTERS,
                    role = Role.STUDENT,
                )

            assertNotNull(result)
            assertEquals("new-oauth-id", user.oauthId)
            verify { userAdaptor.save(user) }
        }

        @Test
        fun `이메일로 유저를 찾지 못하면 null을 반환한다`() {
            every { userAdaptor.findByEmailOrNull("unknown@example.com") } returns null

            val result =
                userDomainService.linkOAuthAndEnsureRole(
                    email = "unknown@example.com",
                    oauthId = "oauth-id",
                    platform = Platform.SUPPORTERS,
                    role = Role.STUDENT,
                )

            assertNull(result)
        }
    }

    @Nested
    inner class RegisterWithOAuth {
        @Test
        fun `OAuth로 성공적으로 회원가입한다`() {
            val savedUser = mockk<User> { every { id } returns "new-user-id" }
            every { userAdaptor.existsByEmail("new@example.com") } returns false
            every { userAdaptor.save(any()) } returns savedUser
            every { userRoleAdaptor.save(any()) } returns mockk()

            val result =
                userDomainService.registerWithOAuth(
                    oauthId = "oauth-id",
                    authProvider = AuthProvider.GOOGLE,
                    email = "new@example.com",
                    name = "신규유저",
                    phoneNumber = "01012345678",
                    profileImageUrl = null,
                    platform = Platform.SUPPORTERS,
                    role = Role.STUDENT,
                )

            assertEquals(savedUser, result)
            verify { userAdaptor.save(any()) }
            verify { userRoleAdaptor.save(any()) }
        }

        @Test
        fun `이미 존재하는 이메일로 OAuth 회원가입하면 UserAlreadyExistsException이 발생한다`() {
            every { userAdaptor.existsByEmail("existing@example.com") } returns true

            assertThrows<UserAlreadyExistsException> {
                userDomainService.registerWithOAuth(
                    oauthId = "oauth-id",
                    authProvider = AuthProvider.GOOGLE,
                    email = "existing@example.com",
                    name = "테스트",
                    phoneNumber = "01012345678",
                    profileImageUrl = null,
                    platform = Platform.SUPPORTERS,
                    role = Role.STUDENT,
                )
            }
        }
    }

    @Nested
    inner class EnsureUserRole {
        @Test
        fun `이미 역할이 존재하면 save를 호출하지 않는다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.SUPPORTERS, Role.STUDENT)
            } returns mockk()

            userDomainService.ensureUserRole("user-id", Platform.SUPPORTERS, Role.STUDENT)

            verify(exactly = 0) { userRoleAdaptor.save(any()) }
        }

        @Test
        fun `같은 플랫폼에 TEACHER가 있는 유저에게 STUDENT 역할을 추가하면 ConflictingRoleException이 발생한다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.SUPPORTERS, Role.STUDENT)
            } returns null
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.SUPPORTERS, Role.TEACHER)
            } returns mockk()

            assertThrows<ConflictingRoleException> {
                userDomainService.ensureUserRole("user-id", Platform.SUPPORTERS, Role.STUDENT)
            }
        }

        @Test
        fun `같은 플랫폼에 STUDENT가 있는 유저에게 TEACHER 역할을 추가하면 ConflictingRoleException이 발생한다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.SUPPORTERS, Role.TEACHER)
            } returns null
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.SUPPORTERS, Role.STUDENT)
            } returns mockk()

            assertThrows<ConflictingRoleException> {
                userDomainService.ensureUserRole("user-id", Platform.SUPPORTERS, Role.TEACHER)
            }
        }

        @Test
        fun `다른 플랫폼이면 STUDENT와 TEACHER를 동시에 가질 수 있다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.LMS, Role.STUDENT)
            } returns null
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.LMS, Role.TEACHER)
            } returns null
            every { userRoleAdaptor.save(any()) } returns mockk()

            userDomainService.ensureUserRole("user-id", Platform.LMS, Role.STUDENT)

            verify(exactly = 1) { userRoleAdaptor.save(any()) }
        }

        @Test
        fun `ADMIN 역할은 충돌 검증 없이 추가된다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole("user-id", Platform.SUPPORTERS, Role.ADMIN)
            } returns null
            every { userRoleAdaptor.save(any()) } returns mockk()

            userDomainService.ensureUserRole("user-id", Platform.SUPPORTERS, Role.ADMIN)

            verify(exactly = 1) { userRoleAdaptor.save(any()) }
        }
    }
}
