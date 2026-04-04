package com.sclass.domain.domains.diagnosis.adaptor

import com.sclass.domain.config.QuerydslConfig
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.repository.DiagnosisRepository
import jakarta.persistence.EntityManager
import org.hibernate.Hibernate
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig::class)
class DiagnosisLazyLoadingTest {
    @Autowired
    private lateinit var repository: DiagnosisRepository

    @Autowired
    private lateinit var em: EntityManager

    @Test
    fun `findById 조회 직후 reportData는 초기화되지 않는다`() {
        // given
        val saved =
            repository.save(
                Diagnosis(
                    requestId = "req-lazy-001",
                    studentName = "홍길동",
                    studentPhone = "010-1234-5678",
                    parentPhone = null,
                    requestData = "{}",
                    reportData = "{\"score\":95}",
                ),
            )
        em.flush()
        em.clear() // 1차 캐시 제거 → 재조회 시 DB hit

        // when
        val loaded = repository.findById(saved.id).get()

        // then - reportData는 아직 로드되지 않음
        assertFalse(
            Hibernate.isPropertyInitialized(loaded, "reportData"),
            "reportData는 접근 전까지 초기화되지 않아야 한다",
        )
    }

    @Test
    fun `reportData에 접근하면 초기화된다`() {
        // given
        val saved =
            repository.save(
                Diagnosis(
                    requestId = "req-lazy-002",
                    studentName = "홍길동",
                    studentPhone = "010-1234-5678",
                    parentPhone = null,
                    requestData = "{}",
                    reportData = "{\"score\":95}",
                ),
            )
        em.flush()
        em.clear()

        // when
        val loaded = repository.findById(saved.id).get()
        loaded.reportData // 접근 → lazy load 트리거

        // then
        assertTrue(
            Hibernate.isPropertyInitialized(loaded, "reportData"),
            "reportData에 접근 후에는 초기화되어야 한다",
        )
    }
}
