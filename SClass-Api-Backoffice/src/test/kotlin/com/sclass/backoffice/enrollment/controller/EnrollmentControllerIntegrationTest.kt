package com.sclass.backoffice.enrollment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.backoffice.config.ApiIntegrationTest
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.repository.CourseRepository
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.repository.EnrollmentRepository
import com.sclass.domain.domains.lesson.repository.LessonRepository
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.repository.ProductRepository
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.repository.TeacherRepository
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.repository.UserRepository
import com.sclass.domain.domains.user.repository.UserRoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class EnrollmentControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userRoleRepository: UserRoleRepository

    @Autowired
    private lateinit var teacherRepository: TeacherRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var enrollmentRepository: EnrollmentRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var lessonRepository: LessonRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var aesTokenEncryptor: AesTokenEncryptor

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private lateinit var adminToken: String
    private lateinit var teacherUser: User
    private lateinit var studentUser: User
    private lateinit var product: CourseProduct
    private lateinit var enrollment: Enrollment

    @BeforeEach
    fun setUp() {
        lessonRepository.deleteAll()
        courseRepository.deleteAll()
        enrollmentRepository.deleteAll()
        teacherRepository.deleteAll()
        productRepository.deleteAll()
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

        teacherUser =
            userRepository.save(
                User(
                    email = "teacher@sclass.com",
                    name = "선생님",
                    authProvider = AuthProvider.EMAIL,
                    hashedPassword = "hashed",
                ),
            )
        teacherRepository.save(Teacher(user = teacherUser))

        product =
            productRepository.save(
                CourseProduct(
                    name = "매칭형 수학 코스",
                    priceWon = 300000,
                    totalLessons = 4,
                    curriculum = "기본 커리큘럼",
                    requiresMatching = true,
                ),
            ) as CourseProduct

        enrollment =
            enrollmentRepository.save(
                Enrollment
                    .createForPurchase(
                        productId = product.id,
                        studentUserId = studentUser.id,
                        tuitionAmountWon = product.priceWon,
                        paymentId = "payment-id-00000000001",
                    ).apply {
                        markPendingMatch()
                    },
            )

        val jwt =
            jwtTokenProvider.generateAccessToken(
                userId = adminUser.id,
                role = "ADMIN",
            )
        adminToken = "Bearer ${aesTokenEncryptor.encrypt(jwt)}"
    }

    @Nested
    inner class CreateCourseFromEnrollment {
        @Test
        fun `매칭 대기 enrollment로 course 생성 성공 시 active와 lesson이 생성된다`() {
            val body =
                mapOf(
                    "teacherUserId" to teacherUser.id,
                )

            mockMvc
                .perform(
                    post("/api/v1/enrollments/${enrollment.id}/course")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(enrollment.id))
                .andExpect(jsonPath("$.data.productId").value(product.id))
                .andExpect(jsonPath("$.data.status").value(EnrollmentStatus.ACTIVE.name))
                .andExpect(jsonPath("$.data.courseId").isNumber)

            val savedEnrollment = enrollmentRepository.findById(enrollment.id).orElseThrow()
            val savedCourse = courseRepository.findById(savedEnrollment.courseId!!).orElseThrow()
            val lessons = lessonRepository.findAllByEnrollmentIdOrderByLessonNumberAscCreatedAtAsc(enrollment.id)

            assertThat(savedEnrollment.status).isEqualTo(EnrollmentStatus.ACTIVE)
            assertThat(savedCourse.productId).isEqualTo(product.id)
            assertThat(savedCourse.teacherUserId).isEqualTo(teacherUser.id)
            assertThat(savedCourse.status).isEqualTo(CourseStatus.UNLISTED)
            assertThat(savedCourse.totalLessons).isEqualTo(4)
            assertThat(savedCourse.curriculum).isEqualTo("기본 커리큘럼")
            assertThat(lessons).hasSize(4)
            assertThat(lessons.map { it.assignedTeacherUserId }).containsOnly(teacherUser.id)
        }

        @Test
        fun `teacherUserId가 빈 문자열이면 400을 반환한다`() {
            val body =
                mapOf(
                    "teacherUserId" to "",
                )

            mockMvc
                .perform(
                    post("/api/v1/enrollments/${enrollment.id}/course")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isBadRequest)
        }
    }
}
