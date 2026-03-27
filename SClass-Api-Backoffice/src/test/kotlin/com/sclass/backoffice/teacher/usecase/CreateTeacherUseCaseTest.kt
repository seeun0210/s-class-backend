package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.CreateTeacherRequest
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
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

class CreateTeacherUseCaseTest {
    private lateinit var userDomainService: UserDomainService
    private lateinit var teacherDomainService: TeacherDomainService
    private lateinit var useCase: CreateTeacherUseCase

    @BeforeEach
    fun setUp() {
        userDomainService = mockk()
        teacherDomainService = mockk()
        useCase = CreateTeacherUseCase(userDomainService, teacherDomainService)
    }

    @Nested
    inner class Success {
        @Test
        fun `선생님 계정을 생성하고 응답을 반환한다`() {
            val request =
                CreateTeacherRequest(
                    email = "teacher@example.com",
                    name = "홍길동",
                    platform = Platform.SUPPORTERS,
                    phoneNumber = "01012345678",
                )

            val savedUser =
                User(
                    email = "teacher@example.com",
                    name = "홍길동",
                    authProvider = AuthProvider.EMAIL,
                    phoneNumber = "010-1234-5678",
                )
            val teacher = Teacher(user = savedUser)

            val userSlot = slot<User>()
            every {
                userDomainService.register(capture(userSlot), "12345678", Platform.SUPPORTERS, Role.TEACHER)
            } returns savedUser
            every { teacherDomainService.register(savedUser, any<TeacherEducation>()) } returns teacher

            val response = useCase.execute(request)

            assertThat(response.teacherId).isEqualTo(teacher.id)
            assertThat(response.userId).isEqualTo(savedUser.id)
            assertThat(response.email).isEqualTo("teacher@example.com")
            assertThat(response.name).isEqualTo("홍길동")
            assertThat(response.platform).isEqualTo(Platform.SUPPORTERS)
            assertThat(response.phoneNumber).isEqualTo("010-1234-5678")

            assertThat(userSlot.captured.email).isEqualTo("teacher@example.com")
            assertThat(userSlot.captured.name).isEqualTo("홍길동")
            assertThat(userSlot.captured.authProvider).isEqualTo(AuthProvider.EMAIL)

            verify(exactly = 1) { userDomainService.register(any(), "12345678", Platform.SUPPORTERS, Role.TEACHER) }
            verify(exactly = 1) { teacherDomainService.register(savedUser, any<TeacherEducation>()) }
        }
    }

    @Nested
    inner class DuplicateEmail {
        @Test
        fun `이미 존재하는 이메일이면 UserAlreadyExistsException을 던진다`() {
            val request =
                CreateTeacherRequest(
                    email = "existing@example.com",
                    name = "홍길동",
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
