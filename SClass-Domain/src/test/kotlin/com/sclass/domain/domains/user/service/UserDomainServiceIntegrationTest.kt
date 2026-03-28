package com.sclass.domain.domains.user.service

import com.sclass.domain.config.DomainTestConfig
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.ConflictingRoleException
import com.sclass.domain.domains.user.exception.DuplicateUserRoleException
import com.sclass.domain.domains.user.exception.UserAlreadyExistsException
import com.sclass.domain.domains.user.repository.UserRepository
import com.sclass.domain.domains.user.repository.UserRoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(DomainTestConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = ["com.sclass.domain"])
class UserDomainServiceIntegrationTest {
    @Autowired
    private lateinit var userDomainService: UserDomainService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userRoleRepository: UserRoleRepository

    @Test
    fun `registerWithOAuth로 유저를 등록하면 DB에 User와 UserRole이 저장된다`() {
        // given & when
        val user =
            userDomainService.registerWithOAuth(
                oauthId = "google-123",
                authProvider = AuthProvider.GOOGLE,
                email = "test@example.com",
                name = "테스트유저",
                phoneNumber = "01012345678",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )

        // then
        val savedUser = userRepository.findById(user.id).orElseThrow()
        assertThat(savedUser.email).isEqualTo("test@example.com")
        assertThat(savedUser.name).isEqualTo("테스트유저")
        assertThat(savedUser.authProvider).isEqualTo(AuthProvider.GOOGLE)
        assertThat(savedUser.oauthId).isEqualTo("google-123")
        assertThat(savedUser.phoneNumber).isEqualTo("010-1234-5678")

        val roles = userRoleRepository.findAllByUserId(user.id)
        assertThat(roles).hasSize(1)
        assertThat(roles[0].platform).isEqualTo(Platform.SUPPORTERS)
        assertThat(roles[0].role).isEqualTo(Role.STUDENT)
    }

    @Test
    fun `이미 존재하는 이메일로 registerWithOAuth하면 UserAlreadyExistsException이 발생한다`() {
        // given
        userDomainService.registerWithOAuth(
            oauthId = "google-100",
            authProvider = AuthProvider.GOOGLE,
            email = "duplicate@example.com",
            name = "유저1",
            phoneNumber = "01011112222",
            profileImageUrl = null,
            platform = Platform.SUPPORTERS,
            role = Role.STUDENT,
        )

        // when & then
        assertThatThrownBy {
            userDomainService.registerWithOAuth(
                oauthId = "google-200",
                authProvider = AuthProvider.GOOGLE,
                email = "duplicate@example.com",
                name = "유저2",
                phoneNumber = "01033334444",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        }.isInstanceOf(UserAlreadyExistsException::class.java)
    }

    @Test
    fun `findByOAuthOrNull로 존재하는 OAuth 유저를 조회할 수 있다`() {
        // given
        userDomainService.registerWithOAuth(
            oauthId = "kakao-456",
            authProvider = AuthProvider.KAKAO,
            email = "kakao@example.com",
            name = "카카오유저",
            phoneNumber = "01055556666",
            profileImageUrl = null,
            platform = Platform.SUPPORTERS,
            role = Role.STUDENT,
        )

        // when
        val found = userDomainService.findByOAuthOrNull("kakao-456", AuthProvider.KAKAO)

        // then
        assertThat(found).isNotNull
        assertThat(found!!.email).isEqualTo("kakao@example.com")
        assertThat(found.oauthId).isEqualTo("kakao-456")
    }

    @Test
    fun `linkOAuthAndEnsureRole로 기존 이메일 유저에 OAuth를 연결하고 Role을 부여한다`() {
        // given
        userDomainService.registerWithOAuth(
            oauthId = "google-789",
            authProvider = AuthProvider.GOOGLE,
            email = "link@example.com",
            name = "기존유저",
            phoneNumber = "01077778888",
            profileImageUrl = null,
            platform = Platform.SUPPORTERS,
            role = Role.STUDENT,
        )

        // when
        val linked =
            userDomainService.linkOAuthAndEnsureRole(
                email = "link@example.com",
                oauthId = "kakao-new-id",
                platform = Platform.LMS,
                role = Role.TEACHER,
            )

        // then
        assertThat(linked).isNotNull
        assertThat(linked!!.oauthId).isEqualTo("kakao-new-id")

        val roles = userRoleRepository.findAllByUserId(linked.id)
        assertThat(roles).hasSize(2)
        assertThat(roles.map { it.platform to it.role })
            .containsExactlyInAnyOrder(
                Platform.SUPPORTERS to Role.STUDENT,
                Platform.LMS to Role.TEACHER,
            )
    }

    @Test
    fun `ensureUserRole은 이미 존재하는 Role이면 중복 생성하지 않는다`() {
        // given
        val user =
            userDomainService.registerWithOAuth(
                oauthId = "google-idempotent",
                authProvider = AuthProvider.GOOGLE,
                email = "idempotent@example.com",
                name = "멱등유저",
                phoneNumber = "01099990000",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )

        // when
        userDomainService.ensureUserRole(user.id, Platform.SUPPORTERS, Role.STUDENT)
        userDomainService.ensureUserRole(user.id, Platform.SUPPORTERS, Role.STUDENT)

        // then
        val roles = userRoleRepository.findAllByUserId(user.id)
        assertThat(roles).hasSize(1)
    }

    @Test
    fun `addUserRole로 새 역할을 추가한다`() {
        // given
        val user =
            userDomainService.registerWithOAuth(
                oauthId = "google-add-role",
                authProvider = AuthProvider.GOOGLE,
                email = "addrole@example.com",
                name = "역할추가유저",
                phoneNumber = "01011111111",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )

        // when
        val newRole = userDomainService.addUserRole(user.id, Platform.LMS, Role.STUDENT)

        // then
        assertThat(newRole.userId).isEqualTo(user.id)
        assertThat(newRole.platform).isEqualTo(Platform.LMS)
        assertThat(newRole.role).isEqualTo(Role.STUDENT)
        assertThat(newRole.state).isEqualTo(UserRoleState.NORMAL)

        val roles = userRoleRepository.findAllByUserId(user.id)
        assertThat(roles).hasSize(2)
    }

    @Test
    fun `addUserRole로 TEACHER 역할 추가 시 DRAFT 상태로 생성된다`() {
        // given
        val user =
            userDomainService.registerWithOAuth(
                oauthId = "google-teacher-role",
                authProvider = AuthProvider.GOOGLE,
                email = "teacher-role@example.com",
                name = "선생역할유저",
                phoneNumber = "01022222222",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )

        // when
        val newRole = userDomainService.addUserRole(user.id, Platform.LMS, Role.TEACHER)

        // then
        assertThat(newRole.state).isEqualTo(UserRoleState.DRAFT)
    }

    @Test
    fun `addUserRole로 중복 역할 추가 시 DuplicateUserRoleException이 발생한다`() {
        // given
        val user =
            userDomainService.registerWithOAuth(
                oauthId = "google-dup-role",
                authProvider = AuthProvider.GOOGLE,
                email = "duprole@example.com",
                name = "중복역할유저",
                phoneNumber = "01033333333",
                profileImageUrl = null,
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )

        // when & then
        assertThatThrownBy {
            userDomainService.addUserRole(user.id, Platform.SUPPORTERS, Role.STUDENT)
        }.isInstanceOf(DuplicateUserRoleException::class.java)
    }

    @Test
    fun `addUserRole로 같은 플랫폼에 STUDENT와 TEACHER를 동시에 추가하면 ConflictingRoleException이 발생한다`() {
        // given
        val user =
            userDomainService.registerWithOAuth(
                oauthId = "google-conflict-role",
                authProvider = AuthProvider.GOOGLE,
                email = "conflict@example.com",
                name = "충돌역할유저",
                phoneNumber = "01044444444",
                profileImageUrl = null,
                platform = Platform.LMS,
                role = Role.STUDENT,
            )

        // when & then
        assertThatThrownBy {
            userDomainService.addUserRole(user.id, Platform.LMS, Role.TEACHER)
        }.isInstanceOf(ConflictingRoleException::class.java)
    }
}
