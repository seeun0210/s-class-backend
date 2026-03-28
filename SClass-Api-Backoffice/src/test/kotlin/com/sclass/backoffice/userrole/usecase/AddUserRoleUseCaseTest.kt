package com.sclass.backoffice.userrole.usecase

import com.sclass.backoffice.userrole.dto.AddUserRoleRequest
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.UserNotFoundException
import com.sclass.domain.domains.user.service.UserDomainService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class AddUserRoleUseCaseTest {
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var userDomainService: UserDomainService
    private lateinit var addUserRoleUseCase: AddUserRoleUseCase

    @BeforeEach
    fun setUp() {
        userAdaptor = mockk()
        userDomainService = mockk()
        addUserRoleUseCase = AddUserRoleUseCase(userAdaptor, userDomainService)
    }

    @Test
    fun `유저에 새로운 역할을 추가한다`() {
        val request =
            AddUserRoleRequest(
                userId = "user-id-123",
                platform = Platform.LMS,
                role = Role.STUDENT,
            )
        val user = mockk<User>()
        val userRole =
            UserRole(
                id = "role-id-456",
                userId = "user-id-123",
                platform = Platform.LMS,
                role = Role.STUDENT,
                state = UserRoleState.NORMAL,
            )

        every { userAdaptor.findById("user-id-123") } returns user
        every {
            userDomainService.addUserRole("user-id-123", Platform.LMS, Role.STUDENT)
        } returns userRole

        val result = addUserRoleUseCase.execute(request)

        assertAll(
            { assertEquals("role-id-456", result.userRoleId) },
            { assertEquals("user-id-123", result.userId) },
            { assertEquals(Platform.LMS, result.platform) },
            { assertEquals(Role.STUDENT, result.role) },
            { assertEquals(UserRoleState.NORMAL, result.state) },
        )
    }

    @Test
    fun `존재하지 않는 유저에 역할을 추가하면 UserNotFoundException이 발생한다`() {
        val request =
            AddUserRoleRequest(
                userId = "unknown-id",
                platform = Platform.LMS,
                role = Role.STUDENT,
            )

        every { userAdaptor.findById("unknown-id") } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            addUserRoleUseCase.execute(request)
        }
    }
}
