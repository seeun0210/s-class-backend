package com.sclass.domain.domains.product.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COMMISSION")
class CommissionProduct(
    name: String,

    @Column(nullable = true)
    val coinCost: Int,

    // Single Table Inheritance: 서브클래스 전용 컬럼은 DB NULL 허용 필수
    @Column(name = "teacher_payout_amount_won", nullable = true)
    val teacherPayoutAmountWon: Int,
) : Product(name = name, priceWon = 0)
