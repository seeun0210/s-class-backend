package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.BulkCreateTeachersRequest
import com.sclass.backoffice.teacher.dto.CreateTeacherRequest
import com.sclass.backoffice.teacher.dto.CreateTeacherResponse
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.exception.UserAlreadyExistsException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BulkCreateTeachersUseCaseTest {
    private lateinit var createTeacherUseCase: CreateTeacherUseCase
    private lateinit var useCase: BulkCreateTeachersUseCase

    @BeforeEach
    fun setUp() {
        createTeacherUseCase = mockk()
        useCase = BulkCreateTeachersUseCase(createTeacherUseCase)
    }

    private fun request(
        email: String,
        name: String = "선생님",
    ) = CreateTeacherRequest(
        email = email,
        name = name,
        platform = Platform.SUPPORTERS,
        phoneNumber = "01012345678",
    )

    private fun response(email: String) =
        CreateTeacherResponse(
            teacherId = "teacher-id",
            userId = "user-id",
            email = email,
            name = "선생님",
            platform = Platform.SUPPORTERS,
            phoneNumber = "010-1234-5678",
        )

    @Nested
    inner class AllSuccess {
        @Test
        fun `전체 성공 시 successCount가 totalCount와 같다`() {
            every { createTeacherUseCase.execute(any()) } answers {
                response(firstArg<CreateTeacherRequest>().email)
            }

            val result =
                useCase.execute(
                    BulkCreateTeachersRequest(
                        teachers =
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
            every { createTeacherUseCase.execute(any()) } answers {
                val req = firstArg<CreateTeacherRequest>()
                CreateTeacherResponse(
                    teacherId = "tid-${req.email}",
                    userId = "uid-${req.email}",
                    email = req.email,
                    name = req.name,
                    platform = req.platform,
                    phoneNumber = "010-1234-5678",
                )
            }

            val result =
                useCase.execute(
                    BulkCreateTeachersRequest(
                        teachers =
                            listOf(
                                request("a@test.com", "김선생"),
                                request("b@test.com", "이선생"),
                                request("c@test.com", "박선생"),
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
                assertThat(it.data!!.name).isEqualTo("김선생")
                assertThat(it.error).isNull()
            }

            result.results[1].let {
                assertThat(it.row).isEqualTo(2)
                assertThat(it.email).isEqualTo("b@test.com")
                assertThat(it.data!!.name).isEqualTo("이선생")
            }

            result.results[2].let {
                assertThat(it.row).isEqualTo(3)
                assertThat(it.email).isEqualTo("c@test.com")
                assertThat(it.data!!.name).isEqualTo("박선생")
            }
        }
    }

    @Nested
    inner class PartialFailure {
        @Test
        fun `DB 이메일 중복 시 해당 건만 실패하고 건별 결과가 정확하다`() {
            every { createTeacherUseCase.execute(match { it.email == "a@test.com" }) } returns
                response("a@test.com")
            every { createTeacherUseCase.execute(match { it.email == "b@test.com" }) } returns
                response("b@test.com")
            every { createTeacherUseCase.execute(match { it.email == "dup@test.com" }) } throws
                UserAlreadyExistsException()

            val result =
                useCase.execute(
                    BulkCreateTeachersRequest(
                        teachers =
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
            every { createTeacherUseCase.execute(any()) } returns response("unique@test.com")

            val result =
                useCase.execute(
                    BulkCreateTeachersRequest(
                        teachers =
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
            every { createTeacherUseCase.execute(any()) } answers {
                response(firstArg<CreateTeacherRequest>().email)
            }

            val result =
                useCase.execute(
                    BulkCreateTeachersRequest(
                        teachers =
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
