package com.sclass.domain.domains.coin.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.coin.exception.InvalidCoinAmountException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.LocalDateTime

@Entity
@Table(
    name = "coin_lots",
    indexes = [
        Index(name = "idx_coin_lots_user", columnList = "user_id"),
        Index(name = "idx_coin_lots_expire", columnList = "expire_at"),
        Index(name = "idx_coin_lots_enrollment", columnList = "enrollment_id"),
    ],
)
class CoinLot(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(name = "user_id", nullable = false, length = 26)
    val userId: String,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    var remaining: Int,

    @Column(name = "expire_at")
    val expireAt: LocalDateTime? = null,

    @Column(name = "enrollment_id")
    val enrollmentId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    val sourceType: CoinLotSourceType,

    @Column(name = "source_meta", length = 255)
    val sourceMeta: String? = null,

    @Version
    val version: Long = 0,
) : BaseTimeEntity() {
    fun isActive(now: LocalDateTime = LocalDateTime.now()): Boolean {
        val exp = expireAt
        return remaining > 0 && (exp == null || exp.isAfter(now))
    }

    fun consume(want: Int): Int {
        if (want <= 0) throw InvalidCoinAmountException()
        val used = minOf(remaining, want)
        remaining -= used
        return used
    }
}
