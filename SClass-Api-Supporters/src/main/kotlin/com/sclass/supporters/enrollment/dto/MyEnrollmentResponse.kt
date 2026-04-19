package com.sclass.supporters.enrollment.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.product.domain.ProductType
import java.time.LocalDateTime

data class MyEnrollmentResponse(
    val id: Long,
    val courseId: Long?,
    val status: EnrollmentStatus,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val course: CourseSummary?,
    val membership: MembershipSummary?,
) {
    data class CourseSummary(
        val name: String,
        val description: String?,
        val thumbnailUrl: String?,
        val teacherName: String?,
        val courseStatus: CourseStatus,
        val enrollmentDeadLine: LocalDateTime?,
        val startAt: LocalDateTime?,
        val endAt: LocalDateTime?,
    )

    data class MembershipSummary(
        val productId: String,
        val productType: ProductType,
        val productName: String,
        val thumbnailUrl: String?,
    )

    companion object {
        fun from(
            dto: EnrollmentWithCourseDto,
            courseThumbnailUrl: String?,
            membershipThumbnailUrl: String?,
        ): MyEnrollmentResponse {
            val course =
                dto.course?.let { c ->
                    CourseSummary(
                        name = dto.courseProduct?.name ?: "",
                        description = dto.courseProduct?.description,
                        thumbnailUrl = courseThumbnailUrl,
                        teacherName = dto.teacherName,
                        courseStatus = c.status,
                        enrollmentDeadLine = c.enrollmentDeadLine,
                        startAt = c.startAt,
                        endAt = c.endAt,
                    )
                }
            val membership =
                dto.membershipProduct?.let { mp ->
                    MembershipSummary(
                        productId = mp.id,
                        productType =
                            when (mp) {
                                is com.sclass.domain.domains.product.domain.RollingMembershipProduct ->
                                    ProductType.ROLLING_MEMBERSHIP
                                is com.sclass.domain.domains.product.domain.CohortMembershipProduct ->
                                    ProductType.COHORT_MEMBERSHIP
                                else -> error("Unknown MembershipProduct subtype")
                            },
                        productName = mp.name,
                        thumbnailUrl = membershipThumbnailUrl,
                    )
                }
            return MyEnrollmentResponse(
                id = dto.enrollment.id,
                courseId = dto.enrollment.courseId,
                status = dto.enrollment.status,
                enrollmentType = dto.enrollment.enrollmentType,
                tuitionAmountWon = dto.enrollment.tuitionAmountWon,
                startAt = dto.enrollment.startAt,
                endAt = dto.enrollment.endAt,
                course = course,
                membership = membership,
            )
        }
    }
}
