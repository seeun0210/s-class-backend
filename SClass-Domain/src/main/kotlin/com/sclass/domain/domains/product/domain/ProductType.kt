package com.sclass.domain.domains.product.domain

enum class ProductType(
    val entityClass: Class<out Product>,
) {
    COIN(CoinProduct::class.java),
    COURSE(CourseProduct::class.java),
    COMMISSION(CommissionProduct::class.java),
}
