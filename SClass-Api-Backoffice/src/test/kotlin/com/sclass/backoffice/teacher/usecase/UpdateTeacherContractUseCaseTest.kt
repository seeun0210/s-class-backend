package com.sclass.backoffice.teacher.usecase

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherContract
import com.sclass.domain.domains.teacher.exception.TeacherContractDateInvalidException
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
import org.junit.jupiter.api.assertAll
import java.time.LocalDate

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
                    policeCheckAt = LocalDate.of(2025, 3, 1),
                    contractStartDate = LocalDate.of(2025, 4, 1),
                    contractEndDate = LocalDate.of(2026, 3, 31),
                )

            every { teacherAdaptor.findByUserId(user.id) } returns teacher
            every { teacherAdaptor.save(teacher) } returns teacher

            useCase.execute(user.id, contract)

            assertAll(
                { assertThat(teacher.contract?.policeCheckAt).isEqualTo(LocalDate.of(2025, 3, 1)) },
                { assertThat(teacher.contract?.contractStartDate).isEqualTo(LocalDate.of(2025, 4, 1)) },
                { assertThat(teacher.contract?.contractEndDate).isEqualTo(LocalDate.of(2026, 3, 31)) },
            )
            verify(exactly = 1) { teacherAdaptor.save(teacher) }
        }

        @Test
        fun `일부 필드만 보내면 나머지 기존 값이 유지된다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val existingContract =
                TeacherContract(
                    policeCheckAt = LocalDate.of(2025, 3, 1),
                    contractStartDate = LocalDate.of(2025, 4, 1),
                    contractEndDate = LocalDate.of(2026, 3, 31),
                )
            val teacher = Teacher(user = user, contract = existingContract)
            val partialUpdate = TeacherContract(contractEndDate = LocalDate.of(2027, 3, 31))

            every { teacherAdaptor.findByUserId(user.id) } returns teacher
            every { teacherAdaptor.save(teacher) } returns teacher

            useCase.execute(user.id, partialUpdate)

            assertAll(
                { assertThat(teacher.contract?.policeCheckAt).isEqualTo(LocalDate.of(2025, 3, 1)) },
                { assertThat(teacher.contract?.contractStartDate).isEqualTo(LocalDate.of(2025, 4, 1)) },
                { assertThat(teacher.contract?.contractEndDate).isEqualTo(LocalDate.of(2027, 3, 31)) },
            )
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `계약 시작일이 종료일보다 이후이면 TeacherContractDateInvalidException을 던진다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val teacher = Teacher(user = user)
            val contract =
                TeacherContract(
                    contractStartDate = LocalDate.of(2026, 4, 1),
                    contractEndDate = LocalDate.of(2025, 3, 31),
                )

            every { teacherAdaptor.findByUserId(user.id) } returns teacher

            assertThatThrownBy { useCase.execute(user.id, contract) }
                .isInstanceOf(TeacherContractDateInvalidException::class.java)
        }

        @Test
        fun `계약 시작일과 종료일이 같으면 TeacherContractDateInvalidException을 던진다`() {
            val user = User(email = "teacher@example.com", name = "홍길동", authProvider = AuthProvider.EMAIL)
            val teacher = Teacher(user = user)
            val contract =
                TeacherContract(
                    contractStartDate = LocalDate.of(2025, 4, 1),
                    contractEndDate = LocalDate.of(2025, 4, 1),
                )

            every { teacherAdaptor.findByUserId(user.id) } returns teacher

            assertThatThrownBy { useCase.execute(user.id, contract) }
                .isInstanceOf(TeacherContractDateInvalidException::class.java)
        }

        @Test
        fun `존재하지 않는 유저이면 TeacherNotFoundException을 던진다`() {
            val contract = TeacherContract(contractStartDate = LocalDate.of(2025, 4, 1))

            every { teacherAdaptor.findByUserId("invalid-id") } throws TeacherNotFoundException()

            assertThatThrownBy { useCase.execute("invalid-id", contract) }
                .isInstanceOf(TeacherNotFoundException::class.java)
        }
    }
}
