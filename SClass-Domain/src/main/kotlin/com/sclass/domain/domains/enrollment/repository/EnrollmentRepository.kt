package com.sclass.domain.domains.enrollment.repository

import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import org.springframework.data.jpa.repository.JpaRepository

interface EnrollmentRepository :
    JpaRepository<Enrollment, Long>,
    EnrollmentCustomRepository {
    fun findAllByStudentUserId(studentUserId: String): List<Enrollment>

    fun findAllByCourseId(courseId: Long): List<Enrollment>

    fun findAllByCourseIdAndStatus(
        courseId: Long,
        status: EnrollmentStatus,
    ): List<Enrollment>

    fun findAllByCourseIdAndEnrollmentType(
        courseId: Long,
        enrollmentType: EnrollmentType,
    ): List<Enrollment>

    fun findAllByCourseIdAndStudentUserIdAndStatusIn(
        courseId: Long,
        studentUserId: String,
        statuses: Collection<EnrollmentStatus>,
    ): List<Enrollment>

    fun findAllByProductIdAndStudentUserIdAndStatusIn(
        productId: String,
        studentUserId: String,
        statuses: Collection<EnrollmentStatus>,
    ): List<Enrollment>

    // Payment → Enrollment 역조회 (콜백 처리용)
    fun findByPaymentId(paymentId: String): Enrollment?

    fun countByCourseIdAndStatusIn(
        courseId: Long,
        statuses: Collection<EnrollmentStatus>,
    ): Long

    fun countByProductIdAndStatusIn(
        productId: String,
        statuses: Collection<EnrollmentStatus>,
    ): Long
}
