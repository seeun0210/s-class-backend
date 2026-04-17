package com.sclass.domain.domains.commission.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "commission_policies")
class CommissionPolicy(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var coinCost: Int,

    @Column(nullable = false)
    var active: Boolean = true,
) : BaseTimeEntity()
