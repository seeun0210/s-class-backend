package com.sclass.domain.domains.coin.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.exception.CoinPackageNotFoundException
import com.sclass.domain.domains.coin.repository.CoinPackageRepository

@Adaptor
class CoinPackageAdaptor(
    private val coinPackageRepository: CoinPackageRepository,
) {
    fun findById(id: String): CoinPackage = coinPackageRepository.findById(id).orElseThrow { CoinPackageNotFoundException() }

    fun findByIdOrNull(id: String): CoinPackage? = coinPackageRepository.findById(id).orElse(null)

    fun findAllActive(): List<CoinPackage> = coinPackageRepository.findAllByActiveTrue()

    fun save(coinPackage: CoinPackage): CoinPackage = coinPackageRepository.save(coinPackage)
}
