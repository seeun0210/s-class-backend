package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("MEMBERSHIP")
class MembershipProduct(
    name: String,
    priceWon: Int,
    description: String? = null,
    thumbnailFileId: String? = null,

    @Column(name = "period_days", nullable = true)
    var periodDays: Int,

    @Column(name = "max_enrollments", nullable = true)
    var maxEnrollments: Int? = null,

    @Column(name = "coin_package_id", nullable = true, length = 26)
    var coinPackageId: String,
) : Product(
        name = name,
        priceWon = priceWon,
        description = description,
        thumbnailFileId = thumbnailFileId,
    ) {
    fun updateMembership(
        newPeriodDays: Int?,
        newMaxEnrollments: Int?,
        newCoinPackageId: String?,
    ) {
        newPeriodDays?.let { periodDays = it }
        newMaxEnrollments?.let { maxEnrollments = it }
        newCoinPackageId?.let { coinPackageId = it }
    }
}
