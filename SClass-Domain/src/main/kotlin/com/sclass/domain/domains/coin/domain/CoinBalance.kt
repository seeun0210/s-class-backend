package com.sclass.domain.domains.coin.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(name = "coin_balances")
class CoinBalance(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, unique = true, length = 26)
    val userId: String,

    @Column(nullable = false)
    var balance: Int = 0,

    @Column(nullable = false)
    var totalIssued: Int = 0,

    @Column(nullable = false)
    var totalUsed: Int = 0,

    @Version
    val version: Long = 0,
) : BaseTimeEntity() {
    fun issue(amount: Int) {
        balance += amount
        totalIssued += amount
    }

    fun deduct(amount: Int) {
        check(balance >= amount) { "잔액이 부족합니다" }
        balance -= amount
        totalUsed += amount
    }

    fun refund(amount: Int) {
        balance += amount
    }
}
