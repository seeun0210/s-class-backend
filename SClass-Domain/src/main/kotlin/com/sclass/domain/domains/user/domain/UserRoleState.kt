package com.sclass.domain.domains.user.domain

enum class UserRoleState {
    ACTIVE,
    DRAFT,
    PENDING,
    REJECTED,
    ;

    val isActive: Boolean get() = this == ACTIVE
}
