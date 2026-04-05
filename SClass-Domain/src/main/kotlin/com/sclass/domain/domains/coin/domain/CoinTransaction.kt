package com.sclass.domain.domains.coin.domain

import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "coin_transactions")
@EntityListeners(AuditingEntityListener::class)
class CoinTransaction(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, length = 26)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: CoinTransactionType,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    val balanceAfter: Int,

    @Column(length = 26)
    val referenceId: String? = null,

    @Column
    val description: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.MIN,
)
