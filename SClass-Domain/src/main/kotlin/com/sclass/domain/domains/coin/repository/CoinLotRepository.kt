package com.sclass.domain.domains.coin.repository

import com.sclass.domain.domains.coin.domain.CoinLot
import org.springframework.data.jpa.repository.JpaRepository

interface CoinLotRepository :
    JpaRepository<CoinLot, String>,
    CoinLotCustomRepository {
    fun findAllByEnrollmentId(enrollmentId: Long): List<CoinLot>
}
