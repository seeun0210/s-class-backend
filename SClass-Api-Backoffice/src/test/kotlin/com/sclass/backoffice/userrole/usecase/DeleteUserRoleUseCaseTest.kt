package com.sclass.backoffice.userrole.usecase

import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteUserRoleUseCaseTest {
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var deleteUserRoleUseCase: DeleteUserRoleUseCase

    @BeforeEach
    fun setUp() {
        userRoleAdaptor = mockk()
        deleteUserRoleUseCase = DeleteUserRoleUseCase(userRoleAdaptor)
    }

    @Test
    fun `유저 역할을 삭제한다`() {
        val userRole =
            UserRole(
                id = "role-id-123",
                userId = "user-id-456",
                platform = Platform.LMS,
                role = Role.STUDENT,
                state = UserRoleState.NORMAL,
            )

        every { userRoleAdaptor.findById("role-id-123") } returns userRole
        every { userRoleAdaptor.delete("role-id-123") } just runs

        deleteUserRoleUseCase.execute("role-id-123")

        verify { userRoleAdaptor.delete("role-id-123") }
    }

    @Test
    fun `존재하지 않는 역할을 삭제하면 RoleNotFoundException이 발생한다`() {
        every { userRoleAdaptor.findById("unknown-id") } throws RoleNotFoundException()

        assertThrows<RoleNotFoundException> {
            deleteUserRoleUseCase.execute("unknown-id")
        }
    }
}
