package com.sclass.domain.domains.diagnosis.repository

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.domain.domains.diagnosis.domain.QDiagnosis
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class DiagnosisRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : DiagnosisCustomRepository {
    override fun findAll(
        pageable: Pageable,
        status: DiagnosisStatus?,
    ): Page<Diagnosis> {
        val diagnosis = QDiagnosis.diagnosis
        val condition = status?.let { diagnosis.status.eq(it) }
        val orderSpecifiers = pageable.sort.toOrderSpecifiers()

        val content =
            queryFactory
                .selectFrom(diagnosis)
                .where(condition)
                .orderBy(*orderSpecifiers)
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val total =
            queryFactory
                .select(diagnosis.count())
                .from(diagnosis)
                .where(condition)
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    private fun Sort.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        if (isUnsorted) return arrayOf(QDiagnosis.diagnosis.createdAt.desc())
        val path = PathBuilder(Diagnosis::class.java, "diagnosis")
        return map { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC
            OrderSpecifier(direction, path.get(order.property, Comparable::class.java))
        }.toList().toTypedArray()
    }
}
