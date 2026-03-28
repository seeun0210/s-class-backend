package com.sclass.backoffice.teacher.usecase

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherPersonalInfo
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

class UpdateTeacherPersonalInfoUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var useCase: UpdateTeacherPersonalInfoUseCase

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        useCase = UpdateTeacherPersonalInfoUseCase(teacherAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `선생님 개인정보를 수정한다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val teacher = Teacher(user = user)
            val personalInfo =
                TeacherPersonalInfo(
                    address = "서울시 강남구",
                    residentNumber = "900101-1234567",
                    bankAccount = "국민은행 123-456-789",
                )

            every { teacherAdaptor.findByUserId(user.id) } returns teacher
            every { teacherAdaptor.save(teacher) } returns teacher

            useCase.execute(user.id, personalInfo)

            assertThat(teacher.personalInfo).isEqualTo(personalInfo)
            assertThat(teacher.personalInfo?.address).isEqualTo("서울시 강남구")
            assertThat(teacher.personalInfo?.residentNumber).isEqualTo("900101-1234567")
            assertThat(teacher.personalInfo?.bankAccount).isEqualTo("국민은행 123-456-789")
            verify(exactly = 1) { teacherAdaptor.save(teacher) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `존재하지 않는 유저이면 TeacherNotFoundException을 던진다`() {
            val personalInfo = TeacherPersonalInfo(address = "서울시")

            every { teacherAdaptor.findByUserId("invalid-id") } throws TeacherNotFoundException()

            assertThatThrownBy { useCase.execute("invalid-id", personalInfo) }
                .isInstanceOf(TeacherNotFoundException::class.java)
        }
    }
}
