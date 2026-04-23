package com.sclass.domain.domains.enrollment.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
import com.sclass.domain.domains.enrollment.exception.EnrollmentAlreadyExistsException
import com.sclass.domain.domains.enrollment.exception.EnrollmentNotFoundException
import com.sclass.domain.domains.enrollment.repository.EnrollmentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class EnrollmentAdaptor(
    private val enrollmentRepository: EnrollmentRepository,
) {
    fun save(enrollment: Enrollment): Enrollment = enrollmentRepository.save(enrollment)

    fun findById(id: Long): Enrollment = enrollmentRepository.findByIdOrNull(id) ?: throw EnrollmentNotFoundException()

    fun findByIdOrNull(id: Long): Enrollment? = enrollmentRepository.findByIdOrNull(id)

    fun findAllByStudent(studentUserId: String): List<Enrollment> = enrollmentRepository.findAllByStudentUserId(studentUserId)

    fun findAllByStudentWithCourse(studentUserId: String): List<EnrollmentWithCourseDto> =
        enrollmentRepository.findAllByStudentUserIdWithCourse(studentUserId)

    fun findAllByCourseWithStudent(courseId: Long): List<EnrollmentWithStudentDto> =
        enrollmentRepository.findAllByCourseIdWithStudent(courseId)

    fun searchEnrollments(
        studentUserId: String?,
        teacherUserId: String?,
        courseId: Long?,
        status: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<EnrollmentWithDetailDto> = enrollmentRepository.searchEnrollments(studentUserId, teacherUserId, courseId, status, pageable)

    fun findAllByCourse(courseId: Long): List<Enrollment> = enrollmentRepository.findAllByCourseId(courseId)

    fun findActiveByCourse(courseId: Long): List<Enrollment> =
        enrollmentRepository.findAllByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE)

    fun findByCourseAndType(
        courseId: Long,
        type: EnrollmentType,
    ): List<Enrollment> = enrollmentRepository.findAllByCourseIdAndEnrollmentType(courseId, type)

    fun findLiveEnrollment(
        courseId: Long,
        studentUserId: String,
    ): Enrollment? =
        enrollmentRepository
            .findAllByCourseIdAndStudentUserIdAndStatusIn(
                courseId,
                studentUserId,
                setOf(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE),
            ).firstOrNull()

    fun findResumableEnrollment(
        courseId: Long,
        studentUserId: String,
    ): Enrollment? {
        val live = findLiveEnrollment(courseId, studentUserId) ?: return null
        if (live.status == EnrollmentStatus.ACTIVE) throw EnrollmentAlreadyExistsException()
        return live
    }

    fun findLiveMembershipEnrollment(
        productId: String,
        studentUserId: String,
    ): Enrollment? =
        enrollmentRepository
            .findAllByProductIdAndStudentUserIdAndStatusIn(
                productId,
                studentUserId,
                setOf(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE),
            ).firstOrNull()

    fun findResumableMembershipEnrollment(
        productId: String,
        studentUserId: String,
    ): Enrollment? {
        val live = findLiveMembershipEnrollment(productId, studentUserId) ?: return null
        if (live.status == EnrollmentStatus.ACTIVE) throw EnrollmentAlreadyExistsException()
        return live
    }

    fun findByPaymentId(paymentId: String): Enrollment =
        enrollmentRepository.findByPaymentId(paymentId) ?: throw EnrollmentNotFoundException()

    fun findByPaymentIdOrNull(paymentId: String): Enrollment? = enrollmentRepository.findByPaymentId(paymentId)

    fun countLiveEnrollments(courseId: Long): Long =
        enrollmentRepository.countByCourseIdAndStatusIn(
            courseId,
            setOf(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE),
        )

    fun countLiveMembershipEnrollments(productId: String): Long =
        enrollmentRepository.countByProductIdAndStatusIn(
            productId,
            setOf(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE),
        )

    fun countLiveMembershipEnrollmentsByProductIds(productIds: Collection<String>): Map<String, Long> =
        enrollmentRepository
            .countLiveMembershipEnrollmentsByProductIds(productIds)
            .associate { it.productId to it.count }

    fun findPendingPaymentOlderThan(threshold: java.time.LocalDateTime): List<Enrollment> =
        enrollmentRepository.findAllByStatusAndCreatedAtBefore(EnrollmentStatus.PENDING_PAYMENT, threshold)

    fun hasActiveMembershipEnrollment(studentUserId: String): Boolean =
        enrollmentRepository.hasActiveMembershipEnrollment(
            studentUserId = studentUserId,
            now = java.time.LocalDateTime.now(),
        )

    fun hasPendingUnassignedMatchingEnrollment(productId: String): Boolean =
        enrollmentRepository.existsByProductIdAndCourseIdIsNullAndStatusIn(
            productId,
            setOf(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.PENDING_MATCH),
        )

    fun findResumableCourseProductEnrollment(
        productId: String,
        studentUserId: String,
    ): Enrollment? {
        val live = enrollmentRepository.findResumableCourseProductEnrollment(productId, studentUserId) ?: return null
        if (live.status in setOf(EnrollmentStatus.ACTIVE, EnrollmentStatus.PENDING_MATCH)) {
            throw EnrollmentAlreadyExistsException()
        }
        return live
    }
}
