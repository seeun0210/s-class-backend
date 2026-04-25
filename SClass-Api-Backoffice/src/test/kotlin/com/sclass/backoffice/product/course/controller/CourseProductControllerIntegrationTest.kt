package com.sclass.backoffice.product.course.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.backoffice.config.ApiIntegrationTest
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.repository.ProductRepository
import com.sclass.domain.domains.teacher.repository.TeacherRepository
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.repository.UserRepository
import com.sclass.domain.domains.user.repository.UserRoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class CourseProductControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var productRepository: ProductRepository

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

    @BeforeEach
    fun setUp() {
        productRepository.deleteAll()
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

        val jwt =
            jwtTokenProvider.generateAccessToken(
                userId = adminUser.id,
                role = "ADMIN",
            )
        adminToken = "Bearer ${aesTokenEncryptor.encrypt(jwt)}"
    }

    @Test
    fun `course product 생성에 성공한다`() {
        val body =
            mapOf(
                "name" to "매칭형 수학 코스",
                "description" to "설명",
                "curriculum" to "커리큘럼",
                "priceWon" to 300000,
                "totalLessons" to 12,
                "requiresMatching" to true,
                "visible" to true,
            )

        mockMvc
            .perform(
                post("/api/v1/course-products")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("매칭형 수학 코스"))
            .andExpect(jsonPath("$.data.requiresMatching").value(true))
            .andExpect(jsonPath("$.data.visible").value(true))

        val saved = productRepository.findAll().single() as CourseProduct
        assertThat(saved.name).isEqualTo("매칭형 수학 코스")
        assertThat(saved.requiresMatching).isTrue()
        assertThat(saved.visible).isTrue()
    }

    @Test
    fun `course product 수정에 성공한다`() {
        val product =
            productRepository.save(
                CourseProduct(
                    name = "기존 코스",
                    priceWon = 200000,
                    totalLessons = 8,
                ),
            ) as CourseProduct
        val body =
            mapOf(
                "name" to "새 코스",
                "curriculum" to "새 커리큘럼",
                "totalLessons" to 16,
                "requiresMatching" to true,
                "visible" to true,
            )

        mockMvc
            .perform(
                put("/api/v1/course-products/${product.id}")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("새 코스"))
            .andExpect(jsonPath("$.data.totalLessons").value(16))
            .andExpect(jsonPath("$.data.requiresMatching").value(true))
            .andExpect(jsonPath("$.data.visible").value(true))
    }

    @Test
    fun `pageable로 course product 목록을 조회한다`() {
        productRepository.save(
            CourseProduct(
                name = "코스 A",
                priceWon = 100000,
                totalLessons = 8,
            ),
        )

        mockMvc
            .perform(
                get("/api/v1/course-products")
                    .header("Authorization", adminToken)
                    .param("page", "0")
                    .param("size", "20"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].name").value("코스 A"))
    }

    @Test
    fun `요청한 sort 기준으로 course product 목록을 조회한다`() {
        productRepository.save(
            CourseProduct(
                name = "코스 B",
                priceWon = 100000,
                totalLessons = 8,
            ),
        )
        productRepository.save(
            CourseProduct(
                name = "코스 A",
                priceWon = 200000,
                totalLessons = 12,
            ),
        )

        mockMvc
            .perform(
                get("/api/v1/course-products")
                    .header("Authorization", adminToken)
                    .param("page", "0")
                    .param("size", "20")
                    .param("sort", "name,asc"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].name").value("코스 A"))
            .andExpect(jsonPath("$.data.content[1].name").value("코스 B"))
    }

    @Test
    fun `name이 비어 있으면 생성은 400을 반환한다`() {
        val body =
            mapOf(
                "name" to "",
                "priceWon" to 300000,
                "totalLessons" to 12,
            )

        mockMvc
            .perform(
                post("/api/v1/course-products")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isBadRequest)
    }
}
