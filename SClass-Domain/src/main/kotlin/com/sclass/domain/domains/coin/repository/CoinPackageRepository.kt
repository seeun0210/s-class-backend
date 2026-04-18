package com.sclass.domain.domains.coin.repository

import com.sclass.domain.domains.coin.domain.CoinPackage
import org.springframework.data.jpa.repository.JpaRepository

interface CoinPackageRepository : JpaRepository<CoinPackage, String> {
    fun findAllByActiveTrue(): List<CoinPackage>
}
