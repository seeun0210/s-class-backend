package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.UpdateStudentStateRequest
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateStudentStateUseCaseTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var useCase: UpdateStudentStateUseCase

    private val userId = "user-id"

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        userRoleAdaptor = mockk()
        useCase = UpdateStudentStateUseCase(studentAdaptor, userRoleAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `학생의 상태를 변경한다`() {
            val user = mockk<User> { every { id } returns "user-id" }
            val student = mockk<Student> { every { this@mockk.user } returns user }
            val userRole =
                UserRole(
                    userId = "user-id",
                    platform = Platform.SUPPORTERS,
                    role = Role.STUDENT,
                    state = UserRoleState.NORMAL,
                )

            every { studentAdaptor.findByUserId(userId) } returns student
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(userId, Platform.SUPPORTERS, Role.STUDENT)
            } returns userRole

            val request =
                UpdateStudentStateRequest(
                    state = UserRoleState.REJECTED,
                    platform = Platform.SUPPORTERS,
                )

            useCase.execute(userId, request)

            assertThat(userRole.state).isEqualTo(UserRoleState.REJECTED)
        }
    }

    @Nested
    inner class StudentNotFound {
        @Test
        fun `학생이 존재하지 않으면 StudentNotFoundException이 발생한다`() {
            every { studentAdaptor.findByUserId(userId) } throws StudentNotFoundException()

            val request =
                UpdateStudentStateRequest(
                    state = UserRoleState.REJECTED,
                    platform = Platform.SUPPORTERS,
                )

            assertThatThrownBy { useCase.execute(userId, request) }
                .isInstanceOf(StudentNotFoundException::class.java)
        }
    }

    @Nested
    inner class RoleNotFound {
        @Test
        fun `해당 플랫폼의 STUDENT 역할이 없으면 RoleNotFoundException이 발생한다`() {
            val user = mockk<User> { every { id } returns "user-id" }
            val student = mockk<Student> { every { this@mockk.user } returns user }

            every { studentAdaptor.findByUserId(userId) } returns student
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(userId, Platform.SUPPORTERS, Role.STUDENT)
            } returns null

            val request =
                UpdateStudentStateRequest(
                    state = UserRoleState.REJECTED,
                    platform = Platform.SUPPORTERS,
                )

            assertThatThrownBy { useCase.execute(userId, request) }
                .isInstanceOf(RoleNotFoundException::class.java)
        }
    }
}
