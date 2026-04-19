package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
abstract class MembershipProduct(
    name: String,
    priceWon: Int,
    description: String? = null,
    thumbnailFileId: String? = null,

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
    fun updateMembershipShared(
        newMaxEnrollments: Int?,
        newCoinPackageId: String?,
    ) {
        newMaxEnrollments?.let { maxEnrollments = it }
        newCoinPackageId?.let { coinPackageId = it }
    }

    abstract fun resolveActivePeriod(now: LocalDateTime): ActivePeriod

    abstract fun validateSaleable(now: LocalDateTime)
}
