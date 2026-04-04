package com.sclass.domain.domains.diagnosis.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.domain.domains.diagnosis.domain.QDiagnosis
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class DiagnosisRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : DiagnosisCustomRepository {
    override fun findAll(
        pageable: Pageable,
        status: DiagnosisStatus?,
    ): Page<Diagnosis> {
        val diagnosis = QDiagnosis.diagnosis
        val condition = status?.let { diagnosis.status.eq(it) }

        val content =
            queryFactory
                .selectFrom(diagnosis)
                .where(condition)
                .orderBy(diagnosis.createdAt.desc())
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
}
