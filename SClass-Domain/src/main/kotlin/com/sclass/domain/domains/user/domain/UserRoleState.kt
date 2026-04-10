package com.sclass.domain.domains.user.domain

enum class UserRoleState {
    NORMAL,
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    ;

    val isActive: Boolean get() = this == NORMAL || this == APPROVED
}
