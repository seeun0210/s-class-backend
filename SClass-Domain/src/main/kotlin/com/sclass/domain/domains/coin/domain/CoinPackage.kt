package com.sclass.domain.domains.coin.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.coin.exception.CoinPackageInvalidStatusTransitionException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "coin_packages")
class CoinPackage(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var priceWon: Int,

    @Column(nullable = false)
    var coinAmount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CoinPackageStatus = CoinPackageStatus.ACTIVE,
) : BaseTimeEntity() {
    fun activate() {
        validateTransition(CoinPackageStatus.ACTIVE)
        this.status = CoinPackageStatus.ACTIVE
    }

    fun deactivate() {
        validateTransition(CoinPackageStatus.INACTIVE)
        this.status = CoinPackageStatus.INACTIVE
    }

    fun archive() {
        validateTransition(CoinPackageStatus.ARCHIVED)
        this.status = CoinPackageStatus.ARCHIVED
    }

    private fun validateTransition(target: CoinPackageStatus) {
        val allowed =
            when (target) {
                CoinPackageStatus.ACTIVE -> setOf(CoinPackageStatus.ACTIVE, CoinPackageStatus.INACTIVE)
                CoinPackageStatus.INACTIVE -> setOf(CoinPackageStatus.ACTIVE, CoinPackageStatus.INACTIVE)
                CoinPackageStatus.ARCHIVED ->
                    setOf(CoinPackageStatus.ACTIVE, CoinPackageStatus.INACTIVE, CoinPackageStatus.ARCHIVED)
            }
        if (status !in allowed) throw CoinPackageInvalidStatusTransitionException()
    }
}
