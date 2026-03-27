package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.BulkCreateStudentsRequest
import com.sclass.backoffice.student.dto.CreateStudentRequest
import com.sclass.backoffice.student.dto.CreateStudentResponse
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.exception.UserAlreadyExistsException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BulkCreateStudentsUseCaseTest {
    private lateinit var createStudentUseCase: CreateStudentUseCase
    private lateinit var useCase: BulkCreateStudentsUseCase

    @BeforeEach
    fun setUp() {
        createStudentUseCase = mockk()
        useCase = BulkCreateStudentsUseCase(createStudentUseCase)
    }

    private fun request(
        email: String,
        name: String = "학생",
    ) = CreateStudentRequest(
        email = email,
        name = name,
        platform = Platform.SUPPORTERS,
        phoneNumber = "01012345678",
    )

    private fun response(email: String) =
        CreateStudentResponse(
            studentId = "student-id",
            userId = "user-id",
            email = email,
            name = "학생",
            platform = Platform.SUPPORTERS,
            phoneNumber = "010-1234-5678",
        )

    @Nested
    inner class AllSuccess {
        @Test
        fun `전체 성공 시 successCount가 totalCount와 같다`() {
            every { createStudentUseCase.execute(any()) } answers {
                response(firstArg<CreateStudentRequest>().email)
            }

            val result =
                useCase.execute(
                    BulkCreateStudentsRequest(
                        students =
                            listOf(
                                request("a@test.com"),
                                request("b@test.com"),
                            ),
                    ),
                )

            assertThat(result.totalCount).isEqualTo(2)
            assertThat(result.successCount).isEqualTo(2)
            assertThat(result.failureCount).isEqualTo(0)
            assertThat(result.results).allMatch { it.success }
        }

        @Test
        fun `성공 건별로 data에 생성 결과가 포함된다`() {
            every { createStudentUseCase.execute(any()) } answers {
                val req = firstArg<CreateStudentRequest>()
                CreateStudentResponse(
                    studentId = "sid-${req.email}",
                    userId = "uid-${req.email}",
                    email = req.email,
                    name = req.name,
                    platform = req.platform,
                    phoneNumber = "010-1234-5678",
                )
            }

            val result =
                useCase.execute(
                    BulkCreateStudentsRequest(
                        students =
                            listOf(
                                request("a@test.com", "김학생"),
                                request("b@test.com", "이학생"),
                                request("c@test.com", "박학생"),
                            ),
                    ),
                )

            assertThat(result.results).hasSize(3)

            result.results[0].let {
                assertThat(it.row).isEqualTo(1)
                assertThat(it.email).isEqualTo("a@test.com")
                assertThat(it.success).isTrue()
                assertThat(it.data).isNotNull
                assertThat(it.data!!.email).isEqualTo("a@test.com")
                assertThat(it.data!!.name).isEqualTo("김학생")
                assertThat(it.error).isNull()
            }

            result.results[1].let {
                assertThat(it.row).isEqualTo(2)
                assertThat(it.email).isEqualTo("b@test.com")
                assertThat(it.data!!.name).isEqualTo("이학생")
            }

            result.results[2].let {
                assertThat(it.row).isEqualTo(3)
                assertThat(it.email).isEqualTo("c@test.com")
                assertThat(it.data!!.name).isEqualTo("박학생")
            }
        }
    }

    @Nested
    inner class PartialFailure {
        @Test
        fun `DB 이메일 중복 시 해당 건만 실패하고 건별 결과가 정확하다`() {
            every { createStudentUseCase.execute(match { it.email == "a@test.com" }) } returns
                response("a@test.com")
            every { createStudentUseCase.execute(match { it.email == "b@test.com" }) } returns
                response("b@test.com")
            every { createStudentUseCase.execute(match { it.email == "dup@test.com" }) } throws
                UserAlreadyExistsException()

            val result =
                useCase.execute(
                    BulkCreateStudentsRequest(
                        students =
                            listOf(
                                request("a@test.com"),
                                request("dup@test.com"),
                                request("b@test.com"),
                            ),
                    ),
                )

            assertThat(result.totalCount).isEqualTo(3)
            assertThat(result.successCount).isEqualTo(2)
            assertThat(result.failureCount).isEqualTo(1)

            result.results[0].let {
                assertThat(it.row).isEqualTo(1)
                assertThat(it.success).isTrue()
                assertThat(it.data).isNotNull
                assertThat(it.error).isNull()
            }

            result.results[1].let {
                assertThat(it.row).isEqualTo(2)
                assertThat(it.success).isFalse()
                assertThat(it.data).isNull()
                assertThat(it.error).isNotBlank()
            }

            result.results[2].let {
                assertThat(it.row).isEqualTo(3)
                assertThat(it.success).isTrue()
                assertThat(it.data).isNotNull
            }
        }
    }

    @Nested
    inner class DuplicateInRequest {
        @Test
        fun `요청 내 이메일 중복 시 해당 건들은 모두 실패한다`() {
            every { createStudentUseCase.execute(any()) } returns response("unique@test.com")

            val result =
                useCase.execute(
                    BulkCreateStudentsRequest(
                        students =
                            listOf(
                                request("dup@test.com"),
                                request("unique@test.com"),
                                request("dup@test.com"),
                            ),
                    ),
                )

            assertThat(result.totalCount).isEqualTo(3)
            assertThat(result.successCount).isEqualTo(1)
            assertThat(result.failureCount).isEqualTo(2)
            assertThat(result.results[0].success).isFalse()
            assertThat(result.results[0].error).contains("중복")
            assertThat(result.results[1].success).isTrue()
            assertThat(result.results[2].success).isFalse()
        }
    }

    @Nested
    inner class RowNumbering {
        @Test
        fun `row 번호는 1부터 시작한다`() {
            every { createStudentUseCase.execute(any()) } answers {
                response(firstArg<CreateStudentRequest>().email)
            }

            val result =
                useCase.execute(
                    BulkCreateStudentsRequest(
                        students =
                            listOf(
                                request("a@test.com"),
                                request("b@test.com"),
                            ),
                    ),
                )

            assertThat(result.results[0].row).isEqualTo(1)
            assertThat(result.results[1].row).isEqualTo(2)
        }
    }
}
