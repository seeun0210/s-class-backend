package com.sclass.domain.domains.token.dto

import com.sclass.domain.domains.user.domain.Role

data class ResolvedRefreshToken(
    val userId: String,
    val tokenId: String,
    val role: Role,
)
