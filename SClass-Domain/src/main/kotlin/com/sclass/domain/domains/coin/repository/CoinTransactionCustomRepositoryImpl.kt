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
                        coinTransaction.amount.sum(),
                        coinTransaction.referenceId,
                        coinTransaction.description.max(),
                        coinTransaction.createdAt.min(),
                    ),
                ).from(coinTransaction)
                .where(*conditions)
                .groupBy(coinTransaction.type, coinTransaction.referenceId)
                .orderBy(coinTransaction.createdAt.min().desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        // referenceId가 있는 그룹 수 + referenceId가 null인 row 수
        val groupedCount =
            queryFactory
                .select(coinTransaction.referenceId.countDistinct())
                .from(coinTransaction)
                .where(*conditions, coinTransaction.referenceId.isNotNull)
                .fetchOne() ?: 0L

        val ungroupedCount =
            queryFactory
                .select(coinTransaction.count())
                .from(coinTransaction)
                .where(*conditions, coinTransaction.referenceId.isNull)
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, groupedCount + ungroupedCount)
    }
}
