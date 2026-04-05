package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COIN")
class CoinProduct(
    name: String,
    priceWon: Int,

    @Column(nullable = false)
    val coinAmount: Int,
) : Product(name = name, priceWon = priceWon)
