package com.sclass.domain.domains.product.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
abstract class Product(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val priceWon: Int,

    @Column(nullable = false)
    var active: Boolean = true,
) : BaseTimeEntity()
