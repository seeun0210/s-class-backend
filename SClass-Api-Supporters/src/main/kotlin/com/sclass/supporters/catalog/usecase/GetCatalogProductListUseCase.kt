package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.domain.ProductCatalogSort
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import com.sclass.supporters.catalog.dto.CatalogProductPageResponse
import com.sclass.supporters.catalog.dto.CatalogProductResponse
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCatalogProductListUseCase(
    private val productAdaptor: ProductAdaptor,
    private val courseAdaptor: CourseAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val coinPackageAdaptor: CoinPackageAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(
        types: List<ProductType>?,
        sort: ProductCatalogSort,
        pageable: Pageable,
    ): CatalogProductPageResponse {
        val page = productAdaptor.findVisibleCatalogProducts(types, sort, pageable)
        val courseProducts = page.content.filterIsInstance<com.sclass.domain.domains.product.domain.CourseProduct>()
        val membershipProducts = page.content.filterIsInstance<MembershipProduct>()

        val catalogCourseMap =
            courseAdaptor.findAllCatalogCoursesByProductIds(
                courseProducts.map { it.id },
            )
        val membershipLiveCounts =
            enrollmentAdaptor.countLiveMembershipEnrollmentsByProductIds(
                membershipProducts.filter { it.maxEnrollments != null }.map { it.id },
            )
        val coinPackages =
            coinPackageAdaptor.findAllByIds(
                membershipProducts.map { it.coinPackageId }.toSet(),
            )

        return CatalogProductPageResponse.from(
            page.map { product ->
                val membershipRemainingSeats =
                    (product as? MembershipProduct)?.maxEnrollments?.let { max ->
                        (max - (membershipLiveCounts[product.id] ?: 0L)).coerceAtLeast(0L)
                    }
                val coinAmount =
                    (product as? MembershipProduct)
                        ?.let { membership -> coinPackages[membership.coinPackageId]?.coinAmount }
                        ?: 0

                CatalogProductResponse.from(
                    product = product,
                    thumbnailUrl = thumbnailUrlResolver.resolve(product.thumbnailFileId),
                    catalogCourses = catalogCourseMap[product.id].orEmpty(),
                    membershipRemainingSeats = membershipRemainingSeats,
                    coinAmount = coinAmount,
                )
            },
        )
    }
}
