package com.sclass.backoffice.teacherassignment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.backoffice.config.ApiIntegrationTest
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.repository.OrganizationRepository
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.repository.StudentRepository
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.repository.TeacherRepository
import com.sclass.domain.domains.teacherassignment.repository.TeacherAssignmentRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class TeacherAssignmentControllerIntegrationTest {
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
    private lateinit var organizationRepository: OrganizationRepository

    @Autowired
    private lateinit var teacherAssignmentRepository: TeacherAssignmentRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var aesTokenEncryptor: AesTokenEncryptor

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private lateinit var adminToken: String
    private lateinit var studentUser: User
    private lateinit var teacherUser: User
    private lateinit var organization: Organization

    @BeforeEach
    fun setUp() {
        teacherAssignmentRepository.deleteAll()
        teacherRepository.deleteAll()
        studentRepository.deleteAll()
        userRoleRepository.deleteAll()
        userRepository.deleteAll()
        organizationRepository.deleteAll()

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
                    name = "김학생",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashed",
                ),
            )

        teacherUser =
            userRepository.save(
                User(
                    email = "teacher@sclass.com",
                    name = "박선생",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashed",
                ),
            )

        studentRepository.save(Student(user = studentUser))
        teacherRepository.save(Teacher(user = teacherUser))

        organization =
            organizationRepository.save(
                Organization(name = "테스트학원", domain = "test.sclass.com"),
            )

        val jwt =
            jwtTokenProvider.generateAccessToken(
                userId = adminUser.id,
                role = "ADMIN",
                platform = "BACKOFFICE",
            )
        adminToken = "Bearer ${aesTokenEncryptor.encrypt(jwt)}"
    }

    @Nested
    inner class AssignAndSearch {
        @Test
        fun `배정 후 검색 목록에 노출된다`() {
            val assignBody =
                mapOf(
                    "studentUserId" to studentUser.id,
                    "teacherUserId" to teacherUser.id,
                    "platform" to "LMS",
                    "organizationId" to organization.id,
                )

            // 배정
            mockMvc
                .perform(
                    post("/api/v1/teacher-assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignBody)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))

            // 검색 목록에서 확인
            mockMvc
                .perform(
                    get("/api/v1/teacher-assignments")
                        .header("Authorization", adminToken),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].studentName").value("김학생"))
                .andExpect(jsonPath("$.data.content[0].teacherName").value("박선생"))
                .andExpect(jsonPath("$.data.content[0].platform").value("LMS"))
                .andExpect(jsonPath("$.data.content[0].organizationName").value("테스트학원"))
        }
    }

    @Nested
    inner class UnassignAndSearch {
        @Test
        fun `해제 후 검색 목록에서 제거된다`() {
            val assignBody =
                mapOf(
                    "studentUserId" to studentUser.id,
                    "teacherUserId" to teacherUser.id,
                    "platform" to "SUPPORTERS",
                )

            // 배정
            mockMvc
                .perform(
                    post("/api/v1/teacher-assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignBody)),
                ).andExpect(status().isOk)

            // 검색 목록에서 확인 (1건)
            mockMvc
                .perform(
                    get("/api/v1/teacher-assignments")
                        .header("Authorization", adminToken),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.data.totalElements").value(1))

            // 해제
            val unassignBody =
                mapOf(
                    "studentUserId" to studentUser.id,
                    "platform" to "SUPPORTERS",
                )

            mockMvc
                .perform(
                    delete("/api/v1/teacher-assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unassignBody)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))

            // 검색 목록에서 제거 확인 (0건)
            mockMvc
                .perform(
                    get("/api/v1/teacher-assignments")
                        .header("Authorization", adminToken),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.data.totalElements").value(0))
        }
    }
}
