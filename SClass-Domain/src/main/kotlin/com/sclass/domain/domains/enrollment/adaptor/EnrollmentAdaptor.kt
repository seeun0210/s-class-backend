package com.sclass.domain.domains.enrollment.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
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

    fun findByPaymentId(paymentId: String): Enrollment =
        enrollmentRepository.findByPaymentId(paymentId) ?: throw EnrollmentNotFoundException()

    fun findByPaymentIdOrNull(paymentId: String): Enrollment? = enrollmentRepository.findByPaymentId(paymentId)

    fun countLiveEnrollments(courseId: Long): Long =
        enrollmentRepository.countByCourseIdAndStatusIn(
            courseId,
            setOf(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE),
        )
}
