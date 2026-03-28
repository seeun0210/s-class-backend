package com.sclass.backoffice.teacher.usecase

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherEducation
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

class UpdateTeacherEducationUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var useCase: UpdateTeacherEducationUseCase

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        useCase = UpdateTeacherEducationUseCase(teacherAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `선생님 학력 정보를 수정한다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val teacher = Teacher(user = user)
            val education =
                TeacherEducation(
                    majorCategory = MajorCategory.ENGINEERING,
                    university = "서울대학교",
                    major = "컴퓨터공학",
                    highSchool = "한영외고",
                )

            every { teacherAdaptor.findByUserId(user.id) } returns teacher
            every { teacherAdaptor.save(teacher) } returns teacher

            useCase.execute(user.id, education)

            assertThat(teacher.education).isEqualTo(education)
            assertThat(teacher.education?.majorCategory).isEqualTo(MajorCategory.ENGINEERING)
            assertThat(teacher.education?.university).isEqualTo("서울대학교")
            assertThat(teacher.education?.major).isEqualTo("컴퓨터공학")
            assertThat(teacher.education?.highSchool).isEqualTo("한영외고")
            verify(exactly = 1) { teacherAdaptor.save(teacher) }
        }

        @Test
        fun `일부 필드만 보내면 나머지 기존 값이 유지된다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val existingEducation =
                TeacherEducation(
                    majorCategory = MajorCategory.ENGINEERING,
                    university = "서울대학교",
                    major = "컴퓨터공학",
                    highSchool = "한영외고",
                )
            val teacher = Teacher(user = user, education = existingEducation)
            val partialUpdate = TeacherEducation(university = "연세대학교")

            every { teacherAdaptor.findByUserId(user.id) } returns teacher
            every { teacherAdaptor.save(teacher) } returns teacher

            useCase.execute(user.id, partialUpdate)

            assertThat(teacher.education?.majorCategory).isEqualTo(MajorCategory.ENGINEERING)
            assertThat(teacher.education?.university).isEqualTo("연세대학교")
            assertThat(teacher.education?.major).isEqualTo("컴퓨터공학")
            assertThat(teacher.education?.highSchool).isEqualTo("한영외고")
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `존재하지 않는 유저이면 TeacherNotFoundException을 던진다`() {
            val education = TeacherEducation(majorCategory = MajorCategory.ENGINEERING)

            every { teacherAdaptor.findByUserId("invalid-id") } throws TeacherNotFoundException()

            assertThatThrownBy { useCase.execute("invalid-id", education) }
                .isInstanceOf(TeacherNotFoundException::class.java)
        }
    }
}
