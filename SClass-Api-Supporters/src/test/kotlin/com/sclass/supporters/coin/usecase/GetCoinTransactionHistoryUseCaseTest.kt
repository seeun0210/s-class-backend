package com.sclass.supporters.coin.usecase

import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.domain.domains.coin.dto.CoinTransactionGroupDto
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

class GetCoinTransactionHistoryUseCaseTest {
    private lateinit var coinAdaptor: CoinAdaptor
    private lateinit var useCase: GetCoinTransactionHistoryUseCase

    @BeforeEach
    fun setUp() {
        coinAdaptor = mockk()
        useCase = GetCoinTransactionHistoryUseCase(coinAdaptor)
    }

    @Test
    fun `트랜잭션 히스토리를 그룹핑하여 반환한다`() {
        val now = LocalDateTime.of(2026, 4, 19, 12, 0)
        val items =
            listOf(
                CoinTransactionGroupDto(CoinTransactionType.ISSUED, 500, "ref-001", "멤버십 코인 지급", now.minusDays(2)),
                CoinTransactionGroupDto(CoinTransactionType.DEDUCTED, 100, "ref-002", "의뢰 생성 코인 차감", now.minusDays(1)),
            )
        every {
            coinAdaptor.findGroupedTransactions("user-01", null, null, null, PageRequest.of(0, 20))
        } returns PageImpl(items, PageRequest.of(0, 20), 2)

        val result = useCase.execute("user-01", null, null, null, 0, 20)

        assertAll(
            { assertEquals(2, result.content.size) },
            { assertEquals(CoinTransactionType.ISSUED, result.content[0].type) },
            { assertEquals(500, result.content[0].totalAmount) },
            { assertEquals("ref-001", result.content[0].referenceId) },
            { assertEquals(CoinTransactionType.DEDUCTED, result.content[1].type) },
            { assertEquals(100, result.content[1].totalAmount) },
            { assertEquals(2L, result.totalElements) },
            { assertEquals(1, result.totalPages) },
            { assertEquals(0, result.currentPage) },
        )
    }

    @Test
    fun `트랜잭션이 없으면 빈 목록을 반환한다`() {
        every {
            coinAdaptor.findGroupedTransactions(any(), any(), any(), any(), any())
        } returns PageImpl(emptyList(), PageRequest.of(0, 20), 0)

        val result = useCase.execute("user-01", null, null, null, 0, 20)

        assertAll(
            { assertEquals(0, result.content.size) },
            { assertEquals(0L, result.totalElements) },
        )
    }
}
