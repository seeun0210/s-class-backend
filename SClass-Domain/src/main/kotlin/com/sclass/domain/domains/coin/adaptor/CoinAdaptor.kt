package com.sclass.domain.domains.coin.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.coin.domain.CoinBalance
import com.sclass.domain.domains.coin.domain.CoinTransaction
import com.sclass.domain.domains.coin.exception.CoinBalanceNotFoundException
import com.sclass.domain.domains.coin.repository.CoinBalanceRepository
import com.sclass.domain.domains.coin.repository.CoinTransactionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class CoinAdaptor(
    private val coinBalanceRepository: CoinBalanceRepository,
    private val coinTransactionRepository: CoinTransactionRepository,
) {
    fun findBalanceByUserId(userId: String): CoinBalance =
        coinBalanceRepository.findByUserId(userId) ?: throw CoinBalanceNotFoundException()

    fun findBalanceByUserIdOrNull(userId: String): CoinBalance? = coinBalanceRepository.findByUserId(userId)

    fun saveBalance(balance: CoinBalance): CoinBalance = coinBalanceRepository.save(balance)

    fun saveTransaction(transaction: CoinTransaction): CoinTransaction = coinTransactionRepository.save(transaction)

    fun findTransactionsByUserId(
        userId: String,
        pageable: Pageable,
    ): Page<CoinTransaction> = coinTransactionRepository.findAllByUserId(userId, pageable)
}
