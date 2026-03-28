package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.UpdateStudentProfileRequest
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateStudentProfileUseCaseTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var useCase: UpdateStudentProfileUseCase

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        useCase = UpdateStudentProfileUseCase(studentAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `학생 프로필을 수정한다`() {
            val user = User(email = "student@example.com", name = "김학생", authProvider = AuthProvider.EMAIL)
            val student = Student(user = user)
            val request =
                UpdateStudentProfileRequest(
                    grade = Grade.HIGH_1,
                    school = "한영외고",
                    parentPhoneNumber = "010-9876-5432",
                )

            every { studentAdaptor.findByUserId(user.id) } returns student
            every { studentAdaptor.save(student) } returns student

            useCase.execute(user.id, request)

            assertThat(student.grade).isEqualTo(Grade.HIGH_1)
            assertThat(student.school).isEqualTo("한영외고")
            assertThat(student.parentPhoneNumber).isEqualTo("010-9876-5432")
            verify(exactly = 1) { studentAdaptor.save(student) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `존재하지 않는 유저이면 StudentNotFoundException을 던진다`() {
            val request = UpdateStudentProfileRequest(grade = Grade.HIGH_1, school = "한영외고", parentPhoneNumber = null)

            every { studentAdaptor.findByUserId("invalid-id") } throws StudentNotFoundException()

            assertThatThrownBy { useCase.execute("invalid-id", request) }
                .isInstanceOf(StudentNotFoundException::class.java)
        }
    }
}
