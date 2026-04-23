package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import com.sclass.supporters.catalog.dto.CatalogProductResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCatalogProductDetailUseCase(
    private val productAdaptor: ProductAdaptor,
    private val courseAdaptor: CourseAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(productId: String): CatalogProductResponse {
        val product =
            productAdaptor.findVisibleCatalogProductById(productId) ?: throw ProductNotFoundException()
        val membershipRemainingSeats =
            (product as? MembershipProduct)?.maxEnrollments?.let { max ->
                (max - enrollmentAdaptor.countLiveMembershipEnrollments(product.id)).coerceAtLeast(0L)
            }
        val coinAmount =
            (product as? MembershipProduct)
                ?.let { membership -> coinPackageAdaptor.findById(membership.coinPackageId).coinAmount }
                ?: 0

        return CatalogProductResponse.from(
            product = product,
            thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
            catalogCourses = courseAdaptor.findAllCatalogCoursesByProductId(productId),
            membershipRemainingSeats = membershipRemainingSeats,
            coinAmount = coinAmount,
        )
    }
}
