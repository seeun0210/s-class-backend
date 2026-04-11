package com.sclass.domain.domains.enrollment.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
import com.sclass.domain.domains.enrollment.exception.EnrollmentNotFoundException
import com.sclass.domain.domains.enrollment.repository.EnrollmentRepository
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class EnrollmentAdaptor(
    private val enrollmentRepository: EnrollmentRepository,
) {
    fun save(enrollment: Enrollment): Enrollment = enrollmentRepository.save(enrollment)

    fun findById(id: Long): Enrollment = enrollmentRepository.findByIdOrNull(id) ?: throw EnrollmentNotFoundException()

    fun findByIdOrNull(id: Long): Enrollment? = enrollmentRepository.findByIdOrNull(id)

    fun findAllByStudent(studentUserId: String): List<Enrollment> = enrollmentRepository.findAllByStudentUserId(studentUserId)

    fun findAllByCourseWithStudent(courseId: Long): List<EnrollmentWithStudentDto> =
        enrollmentRepository.findAllByCourseIdWithStudent(courseId)

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

    fun findByPaymentId(paymentId: String): Enrollment =
        enrollmentRepository.findByPaymentId(paymentId) ?: throw EnrollmentNotFoundException()

    fun findByPaymentIdOrNull(paymentId: String): Enrollment? = enrollmentRepository.findByPaymentId(paymentId)
}
