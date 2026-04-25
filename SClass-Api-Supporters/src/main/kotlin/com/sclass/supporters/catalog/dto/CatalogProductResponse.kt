package com.sclass.supporters.catalog.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.product.domain.CohortMembershipProduct
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.domain.domains.product.domain.toProductType
import com.sclass.domain.domains.product.exception.UnknownProductTypeException
import com.sclass.domain.domains.teacher.domain.MajorCategory
import java.time.LocalDateTime

data class CatalogProductResponse(
    val productId: String,
    val productType: ProductType,
    val name: String,
    val description: String?,
    val thumbnailUrl: String?,
    val priceWon: Int,
    val course: CourseInfo? = null,
    val membership: MembershipInfo? = null,
) {
    data class CourseInfo(
        val totalLessons: Int,
        val curriculum: String?,
        val requiresMatching: Boolean,
        val courseOptions: List<CourseOption>,
    )

    data class CourseOption(
        val courseId: Long,
        val status: CourseStatus,
        val maxEnrollments: Int?,
        val remainingSeats: Long?,
        val enrollmentStartAt: LocalDateTime?,
        val enrollmentDeadLine: LocalDateTime?,
        val startAt: LocalDateTime?,
        val endAt: LocalDateTime?,
        val teacher: TeacherSummary,
    )

    data class TeacherSummary(
        val userId: String,
        val name: String,
        val selfIntroduction: String?,
        val majorCategory: MajorCategory?,
        val university: String?,
        val major: String?,
    )

    data class MembershipInfo(
        val periodDays: Int? = null,
        val startAt: LocalDateTime? = null,
        val endAt: LocalDateTime? = null,
        val maxEnrollments: Int?,
        val remainingSeats: Long?,
        val coinAmount: Int,
    )

    companion object {
        fun from(
            product: Product,
            thumbnailUrl: String?,
            catalogCourses: List<CatalogCourseDto> = emptyList(),
            membershipRemainingSeats: Long? = null,
            coinAmount: Int = 0,
        ): CatalogProductResponse =
            when (product) {
                is CourseProduct ->
                    CatalogProductResponse(
                        productId = product.id,
                        productType = product.toProductType(),
                        name = product.name,
                        description = product.description,
                        thumbnailUrl = thumbnailUrl,
                        priceWon = product.priceWon,
                        course =
                            CourseInfo(
                                totalLessons = product.totalLessons,
                                curriculum = product.curriculum,
                                requiresMatching = product.requiresMatching,
                                courseOptions = catalogCourses.map { it.toCourseOption() },
                            ),
                    )
                is RollingMembershipProduct ->
                    CatalogProductResponse(
                        productId = product.id,
                        productType = product.toProductType(),
                        name = product.name,
                        description = product.description,
                        thumbnailUrl = thumbnailUrl,
                        priceWon = product.priceWon,
                        membership =
                            MembershipInfo(
                                periodDays = product.periodDays,
                                maxEnrollments = product.maxEnrollments,
                                remainingSeats = membershipRemainingSeats,
                                coinAmount = coinAmount,
                            ),
                    )
                is CohortMembershipProduct ->
                    CatalogProductResponse(
                        productId = product.id,
                        productType = product.toProductType(),
                        name = product.name,
                        description = product.description,
                        thumbnailUrl = thumbnailUrl,
                        priceWon = product.priceWon,
                        membership =
                            MembershipInfo(
                                startAt = product.startAt,
                                endAt = product.endAt,
                                maxEnrollments = product.maxEnrollments,
                                remainingSeats = membershipRemainingSeats,
                                coinAmount = coinAmount,
                            ),
                    )
                else -> throw UnknownProductTypeException()
            }
    }
}

private fun CatalogCourseDto.toCourseOption(): CatalogProductResponse.CourseOption {
    val remainingSeats =
        course.maxEnrollments?.let { max ->
            (max - liveEnrollmentCount).coerceAtLeast(0L)
        }

    return CatalogProductResponse.CourseOption(
        courseId = course.id,
        status = course.status,
        maxEnrollments = course.maxEnrollments,
        remainingSeats = remainingSeats,
        enrollmentStartAt = course.enrollmentStartAt,
        enrollmentDeadLine = course.enrollmentDeadLine,
        startAt = course.startAt,
        endAt = course.endAt,
        teacher =
            CatalogProductResponse.TeacherSummary(
                userId = course.teacherUserId,
                name = teacherUser?.name ?: "",
                selfIntroduction = teacher?.profile?.selfIntroduction,
                majorCategory = teacher?.education?.majorCategory,
                university = teacher?.education?.university,
                major = teacher?.education?.major,
            ),
    )
}
