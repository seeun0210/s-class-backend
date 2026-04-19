package com.sclass.domain.domains.coin.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import com.sclass.domain.domains.coin.domain.CoinTransaction
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.domain.domains.coin.exception.InsufficientCoinException
import com.sclass.domain.domains.coin.exception.InvalidCoinAmountException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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
        expireAt: LocalDateTime? = null,
        enrollmentId: Long? = null,
        sourceType: CoinLotSourceType = CoinLotSourceType.PURCHASE,
        sourceMeta: String? = null,
    ): CoinLot {
        if (amount <= 0) throw InvalidCoinAmountException()
        val lot =
            coinAdaptor.saveLot(
                CoinLot(
                    userId = userId,
                    amount = amount,
                    remaining = amount,
                    expireAt = expireAt,
                    enrollmentId = enrollmentId,
                    sourceType = sourceType,
                    sourceMeta = sourceMeta,
                ),
            )
        coinAdaptor.saveTransaction(
            CoinTransaction(
                userId = userId,
                type = CoinTransactionType.ISSUED,
                amount = amount,
                balanceAfter = coinAdaptor.sumActiveLots(userId),
                referenceId = referenceId,
                description = description,
                lotId = lot.id,
            ),
        )
        return lot
    }

    @Transactional
    fun deduct(
        userId: String,
        amount: Int,
        referenceId: String? = null,
        description: String? = null,
    ) {
        if (amount <= 0) throw InvalidCoinAmountException()
        val now = LocalDateTime.now()
        val lots = coinAdaptor.findActiveLotsForUpdate(userId, now)
        val available = lots.sumOf { it.remaining }
        if (available < amount) throw InsufficientCoinException()

        var left = amount
        val touched = mutableListOf<Pair<CoinLot, Int>>()
        for (lot in lots) {
            if (left <= 0) break
            val used = lot.consume(left)
            left -= used
            touched += lot to used
        }
        coinAdaptor.saveLots(touched.map { it.first })

        val balanceAfter = coinAdaptor.sumActiveLots(userId, now)
        coinAdaptor.saveTransactions(
            touched.map { (lot, used) ->
                CoinTransaction(
                    userId = userId,
                    type = CoinTransactionType.DEDUCTED,
                    amount = used,
                    balanceAfter = balanceAfter,
                    referenceId = referenceId,
                    description = description,
                    lotId = lot.id,
                )
            },
        )
    }

    @Transactional
    fun refund(
        userId: String,
        amount: Int,
        referenceId: String? = null,
        description: String? = null,
        enrollmentId: Long? = null,
        expireAt: LocalDateTime? = null,
    ): CoinLot {
        if (amount <= 0) throw InvalidCoinAmountException()
        val lot =
            coinAdaptor.saveLot(
                CoinLot(
                    userId = userId,
                    amount = amount,
                    remaining = amount,
                    expireAt = expireAt,
                    enrollmentId = enrollmentId,
                    sourceType = CoinLotSourceType.REFUND,
                ),
            )
        coinAdaptor.saveTransaction(
            CoinTransaction(
                userId = userId,
                type = CoinTransactionType.REFUNDED,
                amount = amount,
                balanceAfter = coinAdaptor.sumActiveLots(userId),
                referenceId = referenceId,
                description = description,
                lotId = lot.id,
            ),
        )
        return lot
    }

    @Transactional(readOnly = true)
    fun getBalance(userId: String): Int = coinAdaptor.sumActiveLots(userId)
}
