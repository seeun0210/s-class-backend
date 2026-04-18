package com.sclass.domain.domains.course.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.domains.course.exception.CourseInvalidStatusTransitionException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "courses",
    indexes = [
        Index(name = "idx_courses_teacher", columnList = "teacher_user_id"),
        Index(name = "idx_courses_product", columnList = "product_id"),
        Index(name = "idx_courses_status", columnList = "status"),
    ],
)
class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "product_id", nullable = false, length = 26)
    val productId: String,

    @Column(name = "teacher_user_id", nullable = false, length = 26)
    val teacherUserId: String,

    @Column(name = "organization_id", length = 26)
    val organizationId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CourseStatus = CourseStatus.DRAFT,
) : BaseTimeEntity() {
    fun list() {
        validateTransition(CourseStatus.LISTED)
        this.status = CourseStatus.LISTED
    }

    fun unlist() {
        validateTransition(CourseStatus.UNLISTED)
        this.status = CourseStatus.UNLISTED
    }

    fun archive() {
        validateTransition(CourseStatus.ARCHIVED)
        this.status = CourseStatus.ARCHIVED
    }

    private fun validateTransition(target: CourseStatus) {
        val allowed =
            when (target) {
                CourseStatus.DRAFT -> emptySet()
                CourseStatus.LISTED -> setOf(CourseStatus.DRAFT, CourseStatus.UNLISTED)
                CourseStatus.UNLISTED -> setOf(CourseStatus.LISTED)
                CourseStatus.ARCHIVED -> setOf(CourseStatus.DRAFT, CourseStatus.LISTED, CourseStatus.UNLISTED)
            }
        if (status !in allowed) throw CourseInvalidStatusTransitionException()
    }
}
