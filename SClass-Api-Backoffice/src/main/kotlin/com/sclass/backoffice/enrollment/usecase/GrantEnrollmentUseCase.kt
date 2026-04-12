package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.lesson.service.LessonDomainService
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.springframework.transaction.annotation.Transactional

@UseCase
class GrantEnrollmentUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val lessonService: LessonDomainService,
) {
    @Transactional
    @DistributedLock(prefix = "enrollment")
    fun execute(
        adminUserId: String,
        @LockKey studentUserId: String,
        @LockKey courseId: Long,
        grantReason: String,
    ): EnrollmentResponse {
        val course = courseAdaptor.findById(courseId)
        val product =
            productAdaptor.findById(course.productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()

        val enrollment =
            enrollmentAdaptor.save(
                Enrollment.createByGrant(
                    courseId = course.id,
                    studentUserId = studentUserId,
                    grantedByUserId = adminUserId,
                    grantReason = grantReason,
                    teacherPayoutPerLessonWon = product.teacherPayoutPerLessonWon,
                    tuitionAmountWon = product.priceWon,
                ),
            )

        lessonService.createLessonsForEnrollment(
            enrollment,
            course,
            totalLessons = product.totalLessons,
            teacherPayoutPerLessonWon = product.teacherPayoutPerLessonWon,
        )

        return EnrollmentResponse.from(enrollment)
    }
}
