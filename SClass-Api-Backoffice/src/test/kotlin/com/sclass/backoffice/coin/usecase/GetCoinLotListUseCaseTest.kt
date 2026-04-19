package com.sclass.backoffice.coin.usecase

import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

class GetCoinLotListUseCaseTest {
    private lateinit var coinAdaptor: CoinAdaptor
    private lateinit var useCase: GetCoinLotListUseCase

    @BeforeEach
    fun setUp() {
        coinAdaptor = mockk()
        useCase = GetCoinLotListUseCase(coinAdaptor)
    }

    @Test
    fun `CoinLot 목록을 페이지로 반환한다`() {
        val expireAt = LocalDateTime.of(2026, 12, 31, 23, 59)
        val lots =
            listOf(
                CoinLot(
                    id = "lot-id-000000000000000000001",
                    userId = "student-id-0000000000000000001",
                    amount = 300,
                    remaining = 200,
                    enrollmentId = 1L,
                    expireAt = expireAt,
                    sourceType = CoinLotSourceType.ADMIN_GRANT,
                ),
                CoinLot(
                    id = "lot-id-000000000000000000002",
                    userId = "student-id-0000000000000000001",
                    amount = 100,
                    remaining = 100,
                    enrollmentId = null,
                    expireAt = null,
                    sourceType = CoinLotSourceType.ADMIN_GRANT,
                ),
            )
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        every { coinAdaptor.findLotsByUserId("student-id-0000000000000000001", pageable) } returns
            PageImpl(lots, pageable, 2)

        val result = useCase.execute("student-id-0000000000000000001", 0, 20)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertEquals(1, result.totalPages) },
            { assertEquals(0, result.currentPage) },
            { assertEquals("lot-id-000000000000000000001", result.content[0].lotId) },
            { assertEquals(300, result.content[0].amount) },
            { assertEquals(200, result.content[0].remaining) },
            { assertEquals(1L, result.content[0].enrollmentId) },
            { assertEquals(expireAt, result.content[0].expireAt) },
            { assertEquals(CoinLotSourceType.ADMIN_GRANT, result.content[0].sourceType) },
            { assertEquals(null, result.content[1].enrollmentId) },
            { assertEquals(null, result.content[1].expireAt) },
        )
    }

    @Test
    fun `CoinLot이 없으면 빈 페이지를 반환한다`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        every { coinAdaptor.findLotsByUserId("student-id-0000000000000000001", pageable) } returns
            PageImpl(emptyList(), pageable, 0)

        val result = useCase.execute("student-id-0000000000000000001", 0, 20)

        assertAll(
            { assertEquals(0, result.totalElements) },
            { assertEquals(0, result.content.size) },
        )
    }
}
