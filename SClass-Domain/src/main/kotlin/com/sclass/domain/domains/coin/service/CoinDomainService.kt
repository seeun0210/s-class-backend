package com.sclass.domain.domains.coin.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinBalance
import com.sclass.domain.domains.coin.domain.CoinTransaction
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import org.springframework.transaction.annotation.Transactional

@DomainService
class CoinDomainService(
    private val coinAdaptor: CoinAdaptor,
) {
    @Transactional
    fun issue(
        userId: String,
        amount: Int,
        referenceId: String? = null,
        description: String? = null,
    ) {
        val balance =
            coinAdaptor.findBalanceByUserIdOrNull(userId)
                ?: coinAdaptor.saveBalance(CoinBalance(userId = userId))

        balance.issue(amount)
        coinAdaptor.saveBalance(balance)
        coinAdaptor.saveTransaction(
            CoinTransaction(
                userId = userId,
                type = CoinTransactionType.ISSUED,
                amount = amount,
                balanceAfter = balance.balance,
                referenceId = referenceId,
                description = description,
            ),
        )
    }

    @Transactional
    fun deduct(
        userId: String,
        amount: Int,
        referenceId: String? = null,
        description: String? = null,
    ) {
        val balance = coinAdaptor.findBalanceByUserId(userId)

        balance.deduct(amount)
        coinAdaptor.saveBalance(balance)
        coinAdaptor.saveTransaction(
            CoinTransaction(
                userId = userId,
                type = CoinTransactionType.DEDUCTED,
                amount = amount,
                balanceAfter = balance.balance,
                referenceId = referenceId,
                description = description,
            ),
        )
    }

    @Transactional
    fun refund(
        userId: String,
        amount: Int,
        referenceId: String? = null,
        description: String? = null,
    ) {
        val balance = coinAdaptor.findBalanceByUserId(userId)

        balance.refund(amount)
        coinAdaptor.saveBalance(balance)
        coinAdaptor.saveTransaction(
            CoinTransaction(
                userId = userId,
                type = CoinTransactionType.REFUNDED,
                amount = amount,
                balanceAfter = balance.balance,
                referenceId = referenceId,
                description = description,
            ),
        )
    }
}
