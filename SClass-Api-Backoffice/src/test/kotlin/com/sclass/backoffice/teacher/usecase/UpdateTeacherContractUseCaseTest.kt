package com.sclass.backoffice.teacher.usecase

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherContract
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
import java.time.LocalDateTime

class UpdateTeacherContractUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var useCase: UpdateTeacherContractUseCase

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        useCase = UpdateTeacherContractUseCase(teacherAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `선생님 계약 정보를 수정한다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val teacher = Teacher(user = user)
            val contract =
                TeacherContract(
                    policeCheckAt = LocalDateTime.of(2025, 3, 1, 10, 0),
                    contractStartDate = LocalDate.of(2025, 4, 1),
                    contractEndDate = LocalDate.of(2026, 3, 31),
                )

            every { teacherAdaptor.findByUserId(user.id) } returns teacher
            every { teacherAdaptor.save(teacher) } returns teacher

            useCase.execute(user.id, contract)

            assertThat(teacher.contract).isEqualTo(contract)
            assertThat(teacher.contract?.policeCheckAt).isEqualTo(LocalDateTime.of(2025, 3, 1, 10, 0))
            assertThat(teacher.contract?.contractStartDate).isEqualTo(LocalDate.of(2025, 4, 1))
            assertThat(teacher.contract?.contractEndDate).isEqualTo(LocalDate.of(2026, 3, 31))
            verify(exactly = 1) { teacherAdaptor.save(teacher) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `존재하지 않는 유저이면 TeacherNotFoundException을 던진다`() {
            val contract = TeacherContract(contractStartDate = LocalDate.of(2025, 4, 1))

            every { teacherAdaptor.findByUserId("invalid-id") } throws TeacherNotFoundException()

            assertThatThrownBy { useCase.execute("invalid-id", contract) }
                .isInstanceOf(TeacherNotFoundException::class.java)
        }
    }
}
