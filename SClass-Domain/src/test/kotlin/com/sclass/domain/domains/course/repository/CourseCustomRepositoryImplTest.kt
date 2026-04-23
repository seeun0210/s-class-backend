package com.sclass.domain.domains.course.repository

import com.sclass.domain.config.DomainTestConfig
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.product.domain.CourseProduct
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(DomainTestConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = ["com.sclass.domain"])
class CourseCustomRepositoryImplTest {
    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var em: EntityManager

    @Test
    fun `totalLessons 정렬은 course override 값을 우선한다`() {
        val highDefaultProduct =
            CourseProduct(
                name = "높은 기본 차수 코스",
                priceWon = 100000,
                totalLessons = 20,
            )
        em.persist(highDefaultProduct)
        em.persist(
            Course(
                productId = highDefaultProduct.id,
                teacherUserId = "teacher-01",
                status = CourseStatus.DRAFT,
                totalLessons = 5,
            ),
        )

        val lowDefaultProduct =
            CourseProduct(
                name = "낮은 기본 차수 코스",
                priceWon = 100000,
                totalLessons = 10,
            )
        em.persist(lowDefaultProduct)
        em.persist(
            Course(
                productId = lowDefaultProduct.id,
                teacherUserId = "teacher-02",
                status = CourseStatus.DRAFT,
                totalLessons = null,
            ),
        )

        em.flush()
        em.clear()

        val result =
            courseRepository.searchCourses(
                teacherId = null,
                status = null,
                pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "totalLessons")),
            )

        assertEquals(2, result.content.size)
        assertEquals("높은 기본 차수 코스", result.content[0].courseProduct?.name)
        assertEquals(5, result.content[0].course.totalLessons)
        assertEquals("낮은 기본 차수 코스", result.content[1].courseProduct?.name)
        assertEquals(null, result.content[1].course.totalLessons)
        assertEquals(10, result.content[1].courseProduct?.totalLessons)
    }
}
