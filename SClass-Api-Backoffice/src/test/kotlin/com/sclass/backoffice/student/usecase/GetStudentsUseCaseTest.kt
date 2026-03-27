package com.sclass.backoffice.student.usecase

import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.student.dto.StudentWithPlatform
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRoleState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetStudentsUseCaseTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var useCase: GetStudentsUseCase

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        useCase = GetStudentsUseCase(studentAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `검색 조건에 맞는 학생 목록을 반환한다`() {
            val user =
                User(
                    email = "student@example.com",
                    name = "김학생",
                    authProvider = AuthProvider.EMAIL,
                    phoneNumber = "010-1234-5678",
                )
            val student = Student(user = user)
            val pageable = PageRequest.of(0, 20)
            val condition = StudentSearchCondition(name = "김학생")
            val page =
                PageImpl(
                    listOf(
                        StudentWithPlatform(
                            student = student,
                            platform = Platform.SUPPORTERS,
                            state = UserRoleState.NORMAL,
                        ),
                    ),
                    pageable,
                    1L,
                )

            every { studentAdaptor.searchStudents(condition, pageable) } returns page

            val result = useCase.execute(condition, pageable)

            assertThat(result.totalElements).isEqualTo(1L)
            assertThat(result.totalPages).isEqualTo(1)
            assertThat(result.currentPage).isEqualTo(0)
            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].name).isEqualTo("김학생")
            assertThat(result.content[0].email).isEqualTo("student@example.com")
            assertThat(result.content[0].platform).isEqualTo(Platform.SUPPORTERS)
            assertThat(result.content[0].state).isEqualTo(UserRoleState.NORMAL)

            verify(exactly = 1) { studentAdaptor.searchStudents(condition, pageable) }
        }

        @Test
        fun `결과가 없으면 빈 목록을 반환한다`() {
            val pageable = PageRequest.of(0, 20)
            val condition = StudentSearchCondition()
            val page = PageImpl<StudentWithPlatform>(emptyList(), pageable, 0L)

            every { studentAdaptor.searchStudents(condition, pageable) } returns page

            val result = useCase.execute(condition, pageable)

            assertThat(result.totalElements).isEqualTo(0L)
            assertThat(result.content).isEmpty()
        }
    }
}
