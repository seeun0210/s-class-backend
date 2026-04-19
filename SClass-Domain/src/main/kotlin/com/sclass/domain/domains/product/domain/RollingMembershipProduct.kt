package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("ROLLING_MEMBERSHIP")
class RollingMembershipProduct(
    name: String,
    priceWon: Int,
    description: String? = null,
    thumbnailFileId: String? = null,
    maxEnrollments: Int? = null,
    coinPackageId: String,

    @Column(name = "period_days", nullable = true)
    var periodDays: Int,
) : MembershipProduct(
        name = name,
        priceWon = priceWon,
        description = description,
        thumbnailFileId = thumbnailFileId,
        maxEnrollments = maxEnrollments,
        coinPackageId = coinPackageId,
    ) {
    fun updateRolling(newPeriodDays: Int?) {
        newPeriodDays?.let { periodDays = it }
    }

    override fun resolveActivePeriod(now: LocalDateTime): ActivePeriod =
        ActivePeriod(startAt = now, endAt = now.plusDays(periodDays.toLong()))

    override fun validateSaleable(now: LocalDateTime) {
        // rolling 상품은 언제든 판매 가능
    }
}
