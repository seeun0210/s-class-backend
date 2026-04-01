package com.sclass.domain.domains.commission.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.QCommissionFile.commissionFile
import com.sclass.domain.domains.file.domain.QFile.file

class CommissionFileCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CommissionFileCustomRepository {
    override fun findByCommissionId(commissionId: Long): List<CommissionFile> =
        queryFactory
            .selectFrom(commissionFile)
            .join(commissionFile.file, file)
            .fetchJoin()
            .where(commissionFile.commission.id.eq(commissionId))
            .fetch()
}
