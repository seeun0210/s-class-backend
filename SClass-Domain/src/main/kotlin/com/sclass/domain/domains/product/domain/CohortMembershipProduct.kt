package com.sclass.domain.domains.product.domain

import com.sclass.domain.domains.product.exception.CohortSaleEndedException
import com.sclass.domain.domains.product.exception.InvalidCohortPeriodException
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("COHORT_MEMBERSHIP")
class CohortMembershipProduct(
    name: String,
    priceWon: Int,
    description: String? = null,
    thumbnailFileId: String? = null,
    maxEnrollments: Int? = null,
    coinPackageId: String,

    @Column(name = "start_at", nullable = true)
    var startAt: LocalDateTime,

    @Column(name = "end_at", nullable = true)
    var endAt: LocalDateTime,
) : MembershipProduct(
        name = name,
        priceWon = priceWon,
        description = description,
        thumbnailFileId = thumbnailFileId,
        maxEnrollments = maxEnrollments,
        coinPackageId = coinPackageId,
    ) {
    init {
        validatePeriod(startAt, endAt)
    }

    fun updateCohort(
        newStartAt: LocalDateTime?,
        newEndAt: LocalDateTime?,
    ) {
        val nextStart = newStartAt ?: startAt
        val nextEnd = newEndAt ?: endAt
        validatePeriod(nextStart, nextEnd)
        startAt = nextStart
        endAt = nextEnd
    }

    override fun resolveActivePeriod(now: LocalDateTime): ActivePeriod = ActivePeriod(startAt = startAt, endAt = endAt)

    override fun validateSaleable(now: LocalDateTime) {
        if (now.isAfter(endAt)) throw CohortSaleEndedException()
    }

    private fun validatePeriod(
        startAt: LocalDateTime,
        endAt: LocalDateTime,
    ) {
        if (!startAt.isBefore(endAt)) throw InvalidCohortPeriodException()
    }
}
