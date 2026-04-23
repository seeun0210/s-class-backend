package com.sclass.domain.domains.coin.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.domain.CoinPackageStatus
import com.sclass.domain.domains.coin.exception.CoinPackageNotFoundException
import com.sclass.domain.domains.coin.repository.CoinPackageRepository

@Adaptor
class CoinPackageAdaptor(
    private val coinPackageRepository: CoinPackageRepository,
) {
    fun findById(id: String): CoinPackage = coinPackageRepository.findById(id).orElseThrow { CoinPackageNotFoundException() }

    fun findByIdOrNull(id: String): CoinPackage? = coinPackageRepository.findById(id).orElse(null)

    fun findAllByIds(ids: Collection<String>): Map<String, CoinPackage> =
        if (ids.isEmpty()) {
            emptyMap()
        } else {
            coinPackageRepository.findAllById(ids).associateBy { it.id }
        }

    fun findAllActive(): List<CoinPackage> = coinPackageRepository.findAllByStatus(CoinPackageStatus.ACTIVE)

    fun findAll(): List<CoinPackage> = coinPackageRepository.findAll()

    fun save(coinPackage: CoinPackage): CoinPackage = coinPackageRepository.save(coinPackage)
}
