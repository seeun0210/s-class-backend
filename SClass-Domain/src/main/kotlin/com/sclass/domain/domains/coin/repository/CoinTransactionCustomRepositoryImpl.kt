package com.sclass.domain.domains.coin.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.domain.domains.coin.domain.QCoinTransaction.coinTransaction
import com.sclass.domain.domains.coin.dto.CoinTransactionGroupDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class CoinTransactionCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CoinTransactionCustomRepository {
    override fun findGroupedByUser(
        userId: String,
        type: CoinTransactionType?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        pageable: Pageable,
    ): Page<CoinTransactionGroupDto> {
        val conditions =
            listOfNotNull(
                coinTransaction.userId.eq(userId),
                type?.let { coinTransaction.type.eq(it) },
                from?.let { coinTransaction.createdAt.goe(it) },
                to?.let { coinTransaction.createdAt.loe(it) },
            ).toTypedArray()

        val content =
            queryFactory
                .select(
                    Projections.constructor(
                        CoinTransactionGroupDto::class.java,
                        coinTransaction.type,
                        coinTransaction.amount.sum().longValue(),
                        coinTransaction.referenceId,
                        coinTransaction.description.max(),
                        coinTransaction.createdAt.min(),
                    ),
                ).from(coinTransaction)
                .where(*conditions)
                .groupBy(coinTransaction.type, coinTransaction.referenceId)
                .orderBy(coinTransaction.createdAt.min().desc(), coinTransaction.id.min().asc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val total =
            queryFactory
                .select(coinTransaction.type, coinTransaction.referenceId)
                .from(coinTransaction)
                .where(*conditions)
                .groupBy(coinTransaction.type, coinTransaction.referenceId)
                .fetch()
                .size
                .toLong()

        return PageImpl(content, pageable, total)
    }
}
