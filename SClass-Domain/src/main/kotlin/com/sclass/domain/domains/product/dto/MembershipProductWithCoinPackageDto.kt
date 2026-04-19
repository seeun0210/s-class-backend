package com.sclass.domain.domains.product.dto

import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.product.domain.MembershipProduct

data class MembershipProductWithCoinPackageDto(
    val product: MembershipProduct,
    val coinPackage: CoinPackage?,
)
