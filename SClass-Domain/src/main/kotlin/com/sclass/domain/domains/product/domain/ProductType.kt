package com.sclass.domain.domains.product.domain

import com.sclass.domain.domains.product.exception.UnknownProductTypeException

enum class ProductType(
    val entityClass: Class<out Product>,
) {
    COURSE(CourseProduct::class.java),
    ROLLING_MEMBERSHIP(RollingMembershipProduct::class.java),
    COHORT_MEMBERSHIP(CohortMembershipProduct::class.java),
    ;

    companion object {
        fun from(product: Product): ProductType =
            entries.firstOrNull { it.entityClass.isInstance(product) }
                ?: throw UnknownProductTypeException()
    }
}

fun Product.toProductType(): ProductType = ProductType.from(this)
