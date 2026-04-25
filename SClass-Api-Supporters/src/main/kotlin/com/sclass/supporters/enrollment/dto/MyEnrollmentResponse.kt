package com.sclass.supporters.enrollment.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import java.time.LocalDateTime

data class MyEnrollmentResponse(
    val id: Long,
    val courseId: Long?,
    val status: EnrollmentStatus,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val product: ProductSummary?,
    val course: CourseSummary?,
) {
    data class ProductSummary(
        val id: String,
        val type: ProductType,
        val name: String,
        val description: String?,
        val thumbnailUrl: String?,
    )

    data class CourseSummary(
        val id: Long,
        val status: CourseStatus,
        val teacherName: String?,
    )

    companion object {
        fun from(
            dto: EnrollmentWithCourseDto,
            productThumbnailUrl: String?,
        ): MyEnrollmentResponse {
            val product =
                dto.courseProduct?.let { cp ->
                    ProductSummary(
                        id = cp.id,
                        type = ProductType.COURSE,
                        name = cp.name,
                        description = cp.description,
                        thumbnailUrl = productThumbnailUrl,
                    )
                } ?: dto.membershipProduct?.let { mp ->
                    ProductSummary(
                        id = mp.id,
                        type = mp.toProductType(),
                        name = mp.name,
                        description = mp.description,
                        thumbnailUrl = productThumbnailUrl,
                    )
                }
            val course =
                dto.course?.let { c ->
                    CourseSummary(
                        id = c.id,
                        status = c.status,
                        teacherName = dto.teacherName,
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
                product = product,
                course = course,
            )
        }

        private fun MembershipProduct.toProductType(): ProductType =
            when (this) {
                is RollingMembershipProduct -> ProductType.ROLLING_MEMBERSHIP
                is CohortMembershipProduct -> ProductType.COHORT_MEMBERSHIP
                else -> error("Unknown MembershipProduct subtype")
            }
    }
}
