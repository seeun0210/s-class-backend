package com.sclass.domain.domains.product.domain

enum class ProductType(
    val entityClass: Class<out Product>,
) {
    COURSE(CourseProduct::class.java),
}
