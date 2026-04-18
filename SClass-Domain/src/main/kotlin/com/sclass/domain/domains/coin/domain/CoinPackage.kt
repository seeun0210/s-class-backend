package com.sclass.domain.domains.coin.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
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

    @Column(nullable = false)
    var active: Boolean = true,
) : BaseTimeEntity()
