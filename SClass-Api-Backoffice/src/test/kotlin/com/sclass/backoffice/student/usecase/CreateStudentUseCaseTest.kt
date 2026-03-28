package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.CreateStudentRequest
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.exception.UserAlreadyExistsException
import com.sclass.domain.domains.user.service.UserDomainService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateStudentUseCaseTest {
    private lateinit var userDomainService: UserDomainService
    private lateinit var studentDomainService: StudentDomainService
    private lateinit var useCase: CreateStudentUseCase

    @BeforeEach
    fun setUp() {
        userDomainService = mockk()
        studentDomainService = mockk()
        useCase = CreateStudentUseCase(userDomainService, studentDomainService)
    }

    @Nested
    inner class Success {
        @Test
        fun `학생 계정을 생성하고 응답을 반환한다`() {
            val request =
                CreateStudentRequest(
                    email = "student@example.com",
                    name = "김학생",
                    platform = Platform.SUPPORTERS,
                    phoneNumber = "01012345678",
                )

            val savedUser =
                User(
                    email = "student@example.com",
                    name = "김학생",
                    authProvider = AuthProvider.EMAIL,
                    phoneNumber = "010-1234-5678",
                )
            val student = Student(user = savedUser)

            val userSlot = slot<User>()
            every {
                userDomainService.register(capture(userSlot), "12345678", Platform.SUPPORTERS, Role.STUDENT)
            } returns savedUser
            every { studentDomainService.register(savedUser) } returns student

            val response = useCase.execute(request)

            assertThat(response.studentId).isEqualTo(student.id)
            assertThat(response.userId).isEqualTo(savedUser.id)
            assertThat(response.email).isEqualTo("student@example.com")
            assertThat(response.name).isEqualTo("김학생")
            assertThat(response.platform).isEqualTo(Platform.SUPPORTERS)
            assertThat(response.phoneNumber).isEqualTo("010-1234-5678")
            assertThat(response.grade).isNull()
            assertThat(response.school).isNull()

            assertThat(userSlot.captured.email).isEqualTo("student@example.com")
            assertThat(userSlot.captured.name).isEqualTo("김학생")
            assertThat(userSlot.captured.authProvider).isEqualTo(AuthProvider.EMAIL)

            verify(exactly = 1) { userDomainService.register(any(), "12345678", Platform.SUPPORTERS, Role.STUDENT) }
            verify(exactly = 1) { studentDomainService.register(savedUser) }
        }

        @Test
        fun `grade와 school이 있으면 프로필을 업데이트한다`() {
            val request =
                CreateStudentRequest(
                    email = "student@example.com",
                    name = "김학생",
                    platform = Platform.SUPPORTERS,
                    phoneNumber = "01012345678",
                    grade = Grade.HIGH_1,
                    school = "서울고등학교",
                    parentPhoneNumber = "01098765432",
                )

            val savedUser =
                User(
                    email = "student@example.com",
                    name = "김학생",
                    authProvider = AuthProvider.EMAIL,
                    phoneNumber = "010-1234-5678",
                )
            val student = Student(user = savedUser)
            val updatedStudent =
                Student(user = savedUser).apply {
                    updateProfile(Grade.HIGH_1, "서울고등학교", "010-9876-5432")
                }

            every {
                userDomainService.register(any(), "12345678", Platform.SUPPORTERS, Role.STUDENT)
            } returns savedUser
            every { studentDomainService.register(savedUser) } returns student
            every {
                studentDomainService.updateProfile(student, Grade.HIGH_1, "서울고등학교", "010-9876-5432")
            } returns updatedStudent

            val response = useCase.execute(request)

            assertThat(response.grade).isEqualTo(Grade.HIGH_1)
            assertThat(response.school).isEqualTo("서울고등학교")
            verify(exactly = 1) { studentDomainService.updateProfile(student, Grade.HIGH_1, "서울고등학교", "010-9876-5432") }
        }
    }

    @Nested
    inner class DuplicateEmail {
        @Test
        fun `이미 존재하는 이메일이면 UserAlreadyExistsException을 던진다`() {
            val request =
                CreateStudentRequest(
                    email = "existing@example.com",
                    name = "김학생",
                    platform = Platform.LMS,
                    phoneNumber = "01012345678",
                )

            every {
                userDomainService.register(any(), any(), any(), any())
            } throws UserAlreadyExistsException()

            assertThatThrownBy { useCase.execute(request) }
                .isInstanceOf(UserAlreadyExistsException::class.java)
        }
    }
}
