package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidPurchaseTargetException
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidStatusTransitionException
import com.sclass.domain.domains.enrollment.exception.EnrollmentTypeMismatchException
import com.sclass.domain.domains.lesson.service.LessonDomainService
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCourseFromEnrollmentUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val courseAdaptor: CourseAdaptor,
    private val teacherAdaptor: TeacherAdaptor,
    private val lessonDomainService: LessonDomainService,
) {
    @Transactional
    fun execute(
        enrollmentId: Long,
        teacherUserId: String,
    ): EnrollmentResponse {
        val enrollment = enrollmentAdaptor.findById(enrollmentId)
        teacherAdaptor.findByUserId(teacherUserId)

        if (enrollment.status != EnrollmentStatus.PENDING_MATCH || enrollment.courseId != null) {
            throw EnrollmentInvalidStatusTransitionException()
        }

        val productId = enrollment.productId ?: throw EnrollmentTypeMismatchException()

        val product =
            productAdaptor.findById(productId) as?
                CourseProduct
                ?: throw ProductTypeMismatchException()

        if (!product.requiresMatching) {
            throw EnrollmentInvalidPurchaseTargetException()
        }

        val course =
            courseAdaptor.save(
                Course(
                    productId = product.id,
                    teacherUserId = teacherUserId,
                    status = CourseStatus.UNLISTED,
                    maxEnrollments = 1,
                    totalLessons = product.totalLessons,
                    curriculum = product.curriculum,
                ),
            )

        enrollment.assignCourse(course.id)
        enrollmentAdaptor.save(enrollment)

        lessonDomainService.createLessonsForEnrollment(
            enrollment = enrollment,
            teacherUserId = teacherUserId,
            courseName = product.name,
            totalLessons = product.totalLessons,
        )

        return EnrollmentResponse.from(enrollment)
    }
}
