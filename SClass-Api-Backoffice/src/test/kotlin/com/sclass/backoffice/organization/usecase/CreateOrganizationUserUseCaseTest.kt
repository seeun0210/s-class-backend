package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.CreateOrganizationUserRequest
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class CreateOrganizationUserUseCaseTest {
    private lateinit var organizationAdaptor: OrganizationAdaptor
    private lateinit var organizationUserAdaptor: OrganizationUserAdaptor
    private lateinit var userDomainService: UserDomainService
    private lateinit var useCase: CreateOrganizationUserUseCase

    @BeforeEach
    fun setUp() {
        organizationAdaptor = mockk()
        organizationUserAdaptor = mockk()
        userDomainService = mockk()
        useCase = CreateOrganizationUserUseCase(organizationAdaptor, organizationUserAdaptor, userDomainService)
    }

    @Test
    fun `신규 유저를 생성하고 조직에 추가한다`() {
        val organization = Organization(id = 1L, name = "테스트학원", domain = "test.sclass.com")
        val user =
            User(
                id = "user-id",
                email = "new@example.com",
                name = "신규유저",
                authProvider =
                    AuthProvider.EMAIL,
            )
        val organizationUser = OrganizationUser(id = "ou-id", user = user, organization = organization)
        val request =
            CreateOrganizationUserRequest(
                email = "new@example.com",
                name = "신규유저",
                phoneNumber = null,
                platform = Platform.LMS,
                role = Role.TEACHER,
            )

        every { organizationAdaptor.findById(1L) } returns organization
        every { userDomainService.register(any(), "12345678", Platform.LMS, Role.TEACHER) } returns user
        every { organizationUserAdaptor.save(any()) } returns organizationUser

        val result = useCase.execute(1L, request)

        assertAll(
            { assertEquals("ou-id", result.organizationUserId) },
            { assertEquals("user-id", result.userId) },
            { assertEquals("new@example.com", result.email) },
            { assertEquals("신규유저", result.name) },
            { assertEquals(1L, result.organizationId) },
        )
        verify { userDomainService.register(any(), "12345678", Platform.LMS, Role.TEACHER) }
        verify { organizationUserAdaptor.save(any()) }
    }

    @Test
    fun `전화번호 포함하여 신규 유저를 생성한다`() {
        val organization = Organization(id = 1L, name = "테스트학원", domain = "test.sclass.com")
        val user =
            User(
                id = "user-id",
                email = "new@example.com",
                name = "신규유저",
                authProvider = AuthProvider.EMAIL,
                phoneNumber = "010-1234-5678",
            )
        val organizationUser = OrganizationUser(id = "ou-id", user = user, organization = organization)
        val request =
            CreateOrganizationUserRequest(
                email = "new@example.com",
                name = "신규유저",
                phoneNumber = "01012345678",
                platform = Platform.LMS,
                role = Role.STUDENT,
            )

        every { organizationAdaptor.findById(1L) } returns organization
        every { userDomainService.register(any(), "12345678", Platform.LMS, Role.STUDENT) } returns user
        every { organizationUserAdaptor.save(any()) } returns organizationUser

        val result = useCase.execute(1L, request)

        assertAll(
            { assertEquals("user-id", result.userId) },
            { assertEquals("new@example.com", result.email) },
        )
    }
}
