package com.sclass.backoffice.student.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.backoffice.config.ApiIntegrationTest
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.repository.StudentRepository
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class StudentManagementControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userRoleRepository: UserRoleRepository

    @Autowired
    private lateinit var studentRepository: StudentRepository

    @Autowired
    private lateinit var teacherRepository: TeacherRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var aesTokenEncryptor: AesTokenEncryptor

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private lateinit var adminToken: String
    private lateinit var student: Student
    private lateinit var studentUser: User

    @BeforeEach
    fun setUp() {
        teacherRepository.deleteAll()
        studentRepository.deleteAll()
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

        studentUser =
            userRepository.save(
                User(
                    email = "student@sclass.com",
                    name = "학생",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashed",
                ),
            )

        student = studentRepository.save(Student(user = studentUser))

        val jwt =
            jwtTokenProvider.generateAccessToken(
                userId = adminUser.id,
                role = "ADMIN",
                platform = "BACKOFFICE",
            )
        adminToken = "Bearer ${aesTokenEncryptor.encrypt(jwt)}"
    }

    @Nested
    inner class UpdateStudentProfile {
        @Test
        fun `학생 프로필 수정 성공 시 200을 반환한다`() {
            val body =
                mapOf(
                    "grade" to "HIGH_1",
                    "school" to "한영외고",
                    "parentPhoneNumber" to "010-9876-5432",
                )

            mockMvc
                .perform(
                    patch("/api/v1/students/${studentUser.id}/profile")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        fun `존재하지 않는 유저이면 404를 반환한다`() {
            val body = mapOf("grade" to "HIGH_1")

            mockMvc
                .perform(
                    patch("/api/v1/students/invalid-user-id/profile")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `인증 토큰이 없으면 401을 반환한다`() {
            val body = mapOf("grade" to "HIGH_1")

            mockMvc
                .perform(
                    patch("/api/v1/students/${studentUser.id}/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isUnauthorized)
        }
    }
}
