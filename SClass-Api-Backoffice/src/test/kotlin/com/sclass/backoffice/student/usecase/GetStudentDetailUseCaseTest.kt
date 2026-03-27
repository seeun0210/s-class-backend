package com.sclass.backoffice.student.usecase

import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetStudentDetailUseCaseTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var useCase: GetStudentDetailUseCase

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        userRoleAdaptor = mockk()
        useCase = GetStudentDetailUseCase(studentAdaptor, userRoleAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `학생 상세 정보를 조회하여 응답을 반환한다`() {
            val user =
                User(
                    email = "student@example.com",
                    name = "김학생",
                    authProvider = AuthProvider.EMAIL,
                    phoneNumber = "010-1234-5678",
                )
            val student = Student(user = user)
            val roles =
                listOf(
                    UserRole(
                        userId = user.id,
                        platform = Platform.SUPPORTERS,
                        role = Role.STUDENT,
                        state = UserRoleState.NORMAL,
                    ),
                )

            every { studentAdaptor.findById(student.id) } returns student
            every { userRoleAdaptor.findAllByUserId(user.id) } returns roles

            val response = useCase.execute(student.id)

            assertThat(response.id).isEqualTo(student.id)
            assertThat(response.name).isEqualTo("김학생")
            assertThat(response.email).isEqualTo("student@example.com")
            assertThat(response.phoneNumber).isEqualTo("010-1234-5678")
            assertThat(response.roles).hasSize(1)
            assertThat(response.roles[0].platform).isEqualTo(Platform.SUPPORTERS)
            assertThat(response.roles[0].role).isEqualTo(Role.STUDENT)
            assertThat(response.roles[0].state).isEqualTo(UserRoleState.NORMAL)
        }

        @Test
        fun `역할이 없으면 빈 리스트로 응답한다`() {
            val user =
                User(
                    email = "new@example.com",
                    name = "신입학생",
                    authProvider = AuthProvider.EMAIL,
                )
            val student = Student(user = user)

            every { studentAdaptor.findById(student.id) } returns student
            every { userRoleAdaptor.findAllByUserId(user.id) } returns emptyList()

            val response = useCase.execute(student.id)

            assertThat(response.id).isEqualTo(student.id)
            assertThat(response.roles).isEmpty()
        }
    }

    @Nested
    inner class StudentNotFound {
        @Test
        fun `존재하지 않는 studentId이면 StudentNotFoundException이 발생한다`() {
            every { studentAdaptor.findById("unknown-id") } throws StudentNotFoundException()

            assertThatThrownBy { useCase.execute("unknown-id") }
                .isInstanceOf(StudentNotFoundException::class.java)
        }
    }
}
