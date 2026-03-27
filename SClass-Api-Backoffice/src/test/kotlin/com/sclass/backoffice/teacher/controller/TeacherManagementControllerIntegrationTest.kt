package com.sclass.backoffice.teacher.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.backoffice.config.ApiIntegrationTest
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.repository.TeacherRepository
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
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
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var aesTokenEncryptor: AesTokenEncryptor

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private lateinit var adminToken: String
    private lateinit var teacher: Teacher
    private lateinit var teacherUser: User

    @BeforeEach
    fun setUp() {
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
                platform = "BACKOFFICE",
            )
        adminToken = "Bearer ${aesTokenEncryptor.encrypt(jwt)}"
    }

    private fun createTeacherRole(state: UserRoleState): UserRole =
        userRoleRepository.save(
            UserRole(
                userId = teacherUser.id,
                platform = Platform.SUPPORTERS,
                role = Role.TEACHER,
                state = state,
            ),
        )

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
    inner class UpdateState {
        @Test
        fun `APPROVED 요청 시 200을 반환한다`() {
            createTeacherRole(UserRoleState.PENDING)

            val body =
                mapOf(
                    "state" to "APPROVED",
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacher.id}/state")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        fun `REJECTED 요청 시 reason과 함께 200을 반환한다`() {
            createTeacherRole(UserRoleState.PENDING)

            val body =
                mapOf(
                    "state" to "REJECTED",
                    "platform" to "SUPPORTERS",
                    "reason" to "서류 미비",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacher.id}/state")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        fun `REJECTED 요청 시 reason이 없으면 400을 반환한다`() {
            createTeacherRole(UserRoleState.PENDING)

            val body =
                mapOf(
                    "state" to "REJECTED",
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacher.id}/state")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TEACHER_009"))
        }

        @Test
        fun `DRAFT 상태는 허용되지 않아 400을 반환한다`() {
            createTeacherRole(UserRoleState.PENDING)

            val body =
                mapOf(
                    "state" to "DRAFT",
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacher.id}/state")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TEACHER_010"))
        }

        @Test
        fun `PENDING 상태는 허용되지 않아 400을 반환한다`() {
            createTeacherRole(UserRoleState.PENDING)

            val body =
                mapOf(
                    "state" to "PENDING",
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacher.id}/state")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TEACHER_010"))
        }

        @Test
        fun `인증 토큰이 없으면 401을 반환한다`() {
            createTeacherRole(UserRoleState.PENDING)

            val body =
                mapOf(
                    "state" to "APPROVED",
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    patch("/api/v1/teachers/${teacher.id}/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isUnauthorized)
        }
    }
}
