package com.sclass.backoffice.teacher.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.backoffice.config.ApiIntegrationTest
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.student.repository.StudentRepository
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.repository.TeacherRepository
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.repository.UserRepository
import com.sclass.domain.domains.user.repository.UserRoleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class TeacherManagementControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userRoleRepository: UserRoleRepository

    @Autowired
    private lateinit var teacherRepository: TeacherRepository

    @Autowired
    private lateinit var studentRepository: StudentRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var aesTokenEncryptor: AesTokenEncryptor

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private lateinit var adminToken: String
    private lateinit var teacher: Teacher
    private lateinit var teacherUser: User

    @BeforeEach
    fun setUp() {
        studentRepository.deleteAll()
        teacherRepository.deleteAll()
        userRoleRepository.deleteAll()
        userRepository.deleteAll()

        val adminUser =
            userRepository.save(
                User(
                    email = "admin@sclass.com",
                    name = "관리자",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashed",
                ),
            )

        teacherUser =
            userRepository.save(
                User(
                    email = "teacher@sclass.com",
                    name = "선생님",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashed",
                ),
            )

        teacher = teacherRepository.save(Teacher(user = teacherUser))

        val jwt =
            jwtTokenProvider.generateAccessToken(
                userId = adminUser.id,
                role = "ADMIN",
            )
        adminToken = "Bearer ${aesTokenEncryptor.encrypt(jwt)}"
    }

    @Nested
    inner class CreateTeacher {
        @Test
        fun `선생님 계정 생성 성공 시 200을 반환한다`() {
            val body =
                mapOf(
                    "email" to "newteacher@example.com",
                    "name" to "새선생님",
                    "platform" to "SUPPORTERS",
                    "phoneNumber" to "01012345678",
                )

            mockMvc
                .perform(
                    post("/api/v1/teachers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newteacher@example.com"))
                .andExpect(jsonPath("$.data.name").value("새선생님"))
                .andExpect(jsonPath("$.data.platform").value("SUPPORTERS"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.teacherId").exists())
                .andExpect(jsonPath("$.data.userId").exists())
        }

        @Test
        fun `이미 존재하는 이메일이면 409를 반환한다`() {
            val body =
                mapOf(
                    "email" to "teacher@sclass.com",
                    "name" to "중복선생님",
                    "platform" to "SUPPORTERS",
                    "phoneNumber" to "01012345678",
                )

            mockMvc
                .perform(
                    post("/api/v1/teachers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isConflict)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_002"))
        }

        @Test
        fun `이메일이 빈 문자열이면 400을 반환한다`() {
            val body =
                mapOf(
                    "email" to "",
                    "name" to "선생님",
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    post("/api/v1/teachers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `인증 토큰이 없으면 401을 반환한다`() {
            val body =
                mapOf(
                    "email" to "newteacher@example.com",
                    "name" to "새선생님",
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Nested
    inner class BulkCreateTeacher {
        @Test
        fun `일괄 등록 성공 시 건별 결과를 반환한다`() {
            val body =
                mapOf(
                    "teachers" to
                        listOf(
                            mapOf(
                                "email" to "bulk1@example.com",
                                "name" to "선생1",
                                "platform" to "SUPPORTERS",
                                "phoneNumber" to "01011111111",
                            ),
                            mapOf(
                                "email" to "bulk2@example.com",
                                "name" to "선생2",
                                "platform" to "SUPPORTERS",
                                "phoneNumber" to "01022222222",
                            ),
                        ),
                )

            mockMvc
                .perform(
                    post("/api/v1/teachers/bulk")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failureCount").value(0))
                .andExpect(jsonPath("$.data.results[0].row").value(1))
                .andExpect(jsonPath("$.data.results[0].success").value(true))
                .andExpect(jsonPath("$.data.results[0].data.email").value("bulk1@example.com"))
                .andExpect(jsonPath("$.data.results[1].row").value(2))
                .andExpect(jsonPath("$.data.results[1].success").value(true))
                .andExpect(jsonPath("$.data.results[1].data.email").value("bulk2@example.com"))
        }

        @Test
        fun `일부 이메일이 중복이면 해당 건만 실패하고 나머지는 성공한다`() {
            val body =
                mapOf(
                    "teachers" to
                        listOf(
                            mapOf(
                                "email" to "teacher@sclass.com",
                                "name" to "중복선생님",
                                "platform" to "SUPPORTERS",
                                "phoneNumber" to "01011111111",
                            ),
                            mapOf(
                                "email" to "new-bulk@example.com",
                                "name" to "신규선생님",
                                "platform" to "SUPPORTERS",
                                "phoneNumber" to "01022222222",
                            ),
                        ),
                )

            mockMvc
                .perform(
                    post("/api/v1/teachers/bulk")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.results[0].success").value(false))
                .andExpect(jsonPath("$.data.results[0].error").isNotEmpty)
                .andExpect(jsonPath("$.data.results[1].success").value(true))
                .andExpect(jsonPath("$.data.results[1].data.email").value("new-bulk@example.com"))
        }

        @Test
        fun `빈 배열이면 400을 반환한다`() {
            val body = mapOf("teachers" to emptyList<Any>())

            mockMvc
                .perform(
                    post("/api/v1/teachers/bulk")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isBadRequest)
        }
    }

    @Nested
    inner class UpdateTeacherProfile {
        @Test
        fun `선생님 프로필 수정 성공 시 200을 반환한다`() {
            val body = mapOf("birthDate" to "1990-01-01", "selfIntroduction" to "안녕하세요")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/profile")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        fun `존재하지 않는 유저이면 404를 반환한다`() {
            val body = mapOf("birthDate" to "1990-01-01")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/invalid-user-id/profile")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `인증 토큰이 없으면 401을 반환한다`() {
            val body = mapOf("birthDate" to "1990-01-01")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Nested
    inner class UpdateTeacherEducation {
        @Test
        fun `선생님 학력 수정 성공 시 200을 반환한다`() {
            val body =
                mapOf(
                    "majorCategory" to "ENGINEERING",
                    "university" to "서울대학교",
                    "major" to "컴퓨터공학",
                    "highSchool" to "한영외고",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/education")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        fun `존재하지 않는 유저이면 404를 반환한다`() {
            val body = mapOf("university" to "서울대학교")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/invalid-user-id/education")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `인증 토큰이 없으면 401을 반환한다`() {
            val body = mapOf("university" to "서울대학교")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/education")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Nested
    inner class UpdateTeacherPersonalInfo {
        @Test
        fun `선생님 개인정보 수정 성공 시 200을 반환한다`() {
            val body =
                mapOf(
                    "address" to "서울시 강남구",
                    "residentNumber" to "900101-1234567",
                    "bankAccount" to "국민은행 123-456-789",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/personal-info")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        fun `존재하지 않는 유저이면 404를 반환한다`() {
            val body = mapOf("address" to "서울시 강남구")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/invalid-user-id/personal-info")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `인증 토큰이 없으면 401을 반환한다`() {
            val body = mapOf("address" to "서울시 강남구")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/personal-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Nested
    inner class UpdateTeacherContract {
        @Test
        fun `선생님 계약 정보 수정 성공 시 200을 반환한다`() {
            val body =
                mapOf(
                    "contractStartDate" to "2025-04-01",
                    "contractEndDate" to "2026-03-31",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/contract")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        fun `존재하지 않는 유저이면 404를 반환한다`() {
            val body = mapOf("contractStartDate" to "2025-04-01")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/invalid-user-id/contract")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `인증 토큰이 없으면 401을 반환한다`() {
            val body = mapOf("contractStartDate" to "2025-04-01")

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacherUser.id}/contract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isUnauthorized)
        }
    }
}
