package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COMMISSION")
class CommissionProduct(
    name: String,

    @Column(nullable = false)
    val coinCost: Int,
) : Product(name = name, priceWon = 0)
