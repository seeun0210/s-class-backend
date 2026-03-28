package com.sclass.backoffice.organization.usecase

import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.organization.exception.OrganizationUserAlreadyExistsException
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class AddOrganizationUserUseCaseTest {
    private lateinit var organizationAdaptor: OrganizationAdaptor
    private lateinit var organizationUserAdaptor: OrganizationUserAdaptor
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var useCase: AddOrganizationUserUseCase

    @BeforeEach
    fun setUp() {
        organizationAdaptor = mockk()
        organizationUserAdaptor = mockk()
        userAdaptor = mockk()
        useCase = AddOrganizationUserUseCase(organizationAdaptor, organizationUserAdaptor, userAdaptor)
    }

    @Test
    fun `기존 유저를 조직에 추가한다`() {
        val organization = Organization(id = 1L, name = "테스트학원", domain = "fever.aura.co.kr")
        val user =
            User(
                id = "user-id",
                email = "test@example.com",
                name = "테스트",
                authProvider =
                    AuthProvider.EMAIL,
            )
        val organizationUser = OrganizationUser(id = "ou-id", user = user, organization = organization)

        every { organizationAdaptor.findById(1L) } returns organization
        every { userAdaptor.findById("user-id") } returns user
        every { organizationUserAdaptor.existsByUserIdAndOrganizationId("user-id", 1L) } returns false
        every { organizationUserAdaptor.save(any()) } returns organizationUser

        val result = useCase.execute(1L, "user-id")

        assertAll(
            { assertEquals("ou-id", result.organizationUserId) },
            { assertEquals("user-id", result.userId) },
            { assertEquals("test@example.com", result.email) },
            { assertEquals("테스트", result.name) },
            { assertEquals(1L, result.organizationId) },
        )
        verify { organizationUserAdaptor.save(any()) }
    }

    @Test
    fun `이미 소속된 유저를 추가하면 예외가 발생한다`() {
        val organization = Organization(id = 1L, name = "테스트학원", domain = "test.sclass.com")
        val user =
            User(
                id = "user-id",
                email = "test@example.com",
                name = "테스트",
                authProvider =
                    AuthProvider.EMAIL,
            )

        every { organizationAdaptor.findById(1L) } returns organization
        every { userAdaptor.findById("user-id") } returns user
        every { organizationUserAdaptor.existsByUserIdAndOrganizationId("user-id", 1L) } returns true

        assertThrows<OrganizationUserAlreadyExistsException> {
            useCase.execute(1L, "user-id")
        }
    }
}
