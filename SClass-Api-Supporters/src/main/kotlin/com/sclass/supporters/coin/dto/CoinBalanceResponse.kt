package com.sclass.supporters.coin.dto

data class CoinBalanceResponse(
    val balance: Int,
    val totalIssued: Int,
    val totalUsed: Int,
)
