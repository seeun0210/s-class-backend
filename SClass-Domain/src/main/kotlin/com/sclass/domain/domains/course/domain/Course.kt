package com.sclass.domain.domains.course.domain

import com.sclass.domain.common.model.BaseTimeEntity
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
    fun activate() {
        validateTransition(CourseStatus.ACTIVE)
        this.status = CourseStatus.ACTIVE
    }

    fun complete() {
        validateTransition(CourseStatus.COMPLETED)
        this.status = CourseStatus.COMPLETED
    }

    fun cancel() {
        validateTransition(CourseStatus.CANCELLED)
        this.status = CourseStatus.CANCELLED
    }

    private fun validateTransition(target: CourseStatus) {
        val allowed =
            when (target) {
                CourseStatus.DRAFT -> emptySet()
                CourseStatus.ACTIVE -> setOf(CourseStatus.DRAFT)
                CourseStatus.COMPLETED -> setOf(CourseStatus.ACTIVE)
                CourseStatus.CANCELLED -> setOf(CourseStatus.DRAFT, CourseStatus.ACTIVE)
            }
        require(status in allowed) { "Cannot transition from $status to $target" }
    }
}
