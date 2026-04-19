package com.sclass.domain.domains.coin.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinTransaction
import com.sclass.domain.domains.coin.repository.CoinLotRepository
import com.sclass.domain.domains.coin.repository.CoinTransactionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

@Adaptor
class CoinAdaptor(
    private val coinTransactionRepository: CoinTransactionRepository,
    private val coinLotRepository: CoinLotRepository,
) {
    fun saveLot(lot: CoinLot): CoinLot = coinLotRepository.save(lot)

    fun saveLots(lots: List<CoinLot>): List<CoinLot> = coinLotRepository.saveAll(lots)

    fun findActiveLotsForUpdate(
        userId: String,
        now: LocalDateTime = LocalDateTime.now(),
    ): List<CoinLot> = coinLotRepository.findActiveForUpdate(userId, now)

    fun sumActiveLots(
        userId: String,
        now: LocalDateTime = LocalDateTime.now(),
    ): Int = coinLotRepository.sumActive(userId, now)

    fun findLotsByEnrollmentId(enrollmentId: Long): List<CoinLot> = coinLotRepository.findAllByEnrollmentId(enrollmentId)

    fun findExpiringLots(
        now: LocalDateTime,
        limit: Int,
    ): List<CoinLot> = coinLotRepository.findExpiringBefore(now, limit)

    fun saveTransaction(transaction: CoinTransaction): CoinTransaction = coinTransactionRepository.save(transaction)

    fun saveTransactions(transactions: List<CoinTransaction>): List<CoinTransaction> = coinTransactionRepository.saveAll(transactions)

    fun findTransactionsByUserId(
        userId: String,
        pageable: Pageable,
    ): Page<CoinTransaction> = coinTransactionRepository.findAllByUserId(userId, pageable)
}
