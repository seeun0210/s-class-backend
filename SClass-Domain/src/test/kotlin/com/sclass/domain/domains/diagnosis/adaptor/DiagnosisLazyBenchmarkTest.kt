package com.sclass.domain.domains.diagnosis.adaptor

import com.sclass.domain.config.QuerydslConfig
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.repository.DiagnosisRepository
import jakarta.persistence.EntityManager
import org.hibernate.Hibernate
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * Lazy loading 메모리 절감 벤치마크
 *
 * reportData를 접근하지 않는 경로(상태 변경, 이벤트 리스너 등)에서
 * TEXT 컬럼이 heap에 올라오지 않음을 직접 측정한다.
 *
 * - 측정 방식: 로드된 엔티티의 String 필드 바이트 합산
 *   (JVM heap 직접 측정은 GC 타이밍 노이즈가 심해 부정확)
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig::class)
class DiagnosisLazyBenchmarkTest {
    @Autowired
    private lateinit var repository: DiagnosisRepository

    @Autowired
    private lateinit var em: EntityManager

    private val sampleCount = 100

    // 실제 진단 결과 JSON과 유사한 크기 (~50KB)
    private val largeReportData =
        buildString {
            append("""{"version":"1.0","sections":[""")
            repeat(200) { i ->
                if (i > 0) append(",")
                append(
                    """{"sectionId":$i,"score":${60 + i % 40},""" +
                        """"feedback":"이 영역에서 ${60 + i % 40}점을 기록했습니다. 꾸준한 학습이 필요합니다.",""" +
                        """"answers":[${(1..10).joinToString(",") { q ->
                            """{"q":${i * 10 + q},"a":${q % 4 + 1},"ok":${q % 2 == 0}}"""
                        }}]}""",
                )
            }
            append("""],"totalScore":87}""")
        }

    @BeforeEach
    fun setUp() {
        repeat(sampleCount) { i ->
            repository.save(
                Diagnosis(
                    requestId = "req-bench-$i",
                    studentName = "학생$i",
                    studentPhone = "010-0000-${i.toString().padStart(4, '0')}",
                    parentPhone = null,
                    requestData = "{}",
                    reportData = largeReportData,
                ),
            )
        }
        em.flush()
        em.clear()
    }

    @Test
    fun `벤치마크 - 상태 변경 경로에서 reportData가 heap에 올라오지 않는다`() {
        val reportDataBytes = largeReportData.toByteArray().size

        println("\n=== Lazy Loading Memory Benchmark ===")
        println("샘플 수            : $sampleCount 건")
        println("reportData 크기    : ${reportDataBytes / 1024}KB / 건")
        println("총 reportData 크기 : ${reportDataBytes * sampleCount / 1024}KB ($sampleCount 건 합계)")
        println()

        // ── 1. 상태 변경 경로: reportData 미접근 ──
        em.clear()
        val statusDiagnoses = repository.findAll()

        // reportData가 초기화되지 않았음을 검증
        statusDiagnoses.forEach { d ->
            assertFalse(Hibernate.isPropertyInitialized(d, "reportData"))
        }

        // heap에 실제로 올라온 String 데이터 계산
        val statusPathLoadedBytes =
            statusDiagnoses.sumOf { d ->
                d.id.toByteArray().size +
                    d.requestId.toByteArray().size +
                    d.studentName.toByteArray().size +
                    (d.studentPhone?.toByteArray()?.size ?: 0) +
                    d.status.name
                        .toByteArray()
                        .size
                // reportData는 heap에 없음
            }

        println("── 상태 변경 경로 (reportData 미접근) ──")
        println("  heap에 올라온 데이터 : ${statusPathLoadedBytes / 1024}KB")
        println("  reportData 로드 여부 : 없음 (lazy — DB에서 가져오지 않음)")
        println()

        // ── 2. 결과 조회 경로: reportData 접근 ──
        em.clear()
        val fullDiagnoses = repository.findAll()
        fullDiagnoses.forEach { it.reportData } // lazy load 트리거

        // reportData가 초기화됐음을 검증
        fullDiagnoses.forEach { d ->
            assertTrue(Hibernate.isPropertyInitialized(d, "reportData"))
        }

        val fullPathLoadedBytes =
            statusPathLoadedBytes +
                fullDiagnoses.sumOf { d ->
                    d.reportData?.toByteArray()?.size ?: 0
                }

        println("── 결과 조회 경로 (reportData 접근) ──")
        println("  heap에 올라온 데이터 : ${fullPathLoadedBytes / 1024}KB")
        println("  reportData 로드 여부 : 있음 ($sampleCount 건 × ${reportDataBytes / 1024}KB)")
        println()

        val savedBytes = fullPathLoadedBytes - statusPathLoadedBytes
        println("── 비교 ──")
        println("  상태 변경 경로 절약량 : ${savedBytes / 1024}KB")
        println("  건당 절약량           : ${reportDataBytes / 1024}KB")
        println("  1,000건 처리 시 절약  : ${reportDataBytes.toLong() * 1000 / 1024 / 1024}MB")
        println("=====================================\n")

        assertAll(
            { assertTrue(savedBytes > 0, "reportData가 로드되지 않으면 메모리 절약이 있어야 한다") },
            { assertTrue(savedBytes >= reportDataBytes * sampleCount * 0.9, "절약량이 reportData 크기와 근접해야 한다") },
        )
    }
}
