package com.sclass.backoffice.teacher.usecase

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherProfile
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UpdateTeacherProfileUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var useCase: UpdateTeacherProfileUseCase

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        useCase = UpdateTeacherProfileUseCase(teacherAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `선생님 프로필을 수정한다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val teacher = Teacher(user = user)
            val profile = TeacherProfile(birthDate = LocalDate.of(1990, 1, 1), selfIntroduction = "안녕하세요")

            every { teacherAdaptor.findByUserId(user.id) } returns teacher
            every { teacherAdaptor.save(teacher) } returns teacher

            useCase.execute(user.id, profile)

            assertThat(teacher.profile).isEqualTo(profile)
            assertThat(teacher.profile?.birthDate).isEqualTo(LocalDate.of(1990, 1, 1))
            assertThat(teacher.profile?.selfIntroduction).isEqualTo("안녕하세요")
            verify(exactly = 1) { teacherAdaptor.save(teacher) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `존재하지 않는 유저이면 TeacherNotFoundException을 던진다`() {
            val profile = TeacherProfile(birthDate = LocalDate.of(1990, 1, 1))

            every { teacherAdaptor.findByUserId("invalid-id") } throws TeacherNotFoundException()

            assertThatThrownBy { useCase.execute("invalid-id", profile) }
                .isInstanceOf(TeacherNotFoundException::class.java)
        }
    }
}
