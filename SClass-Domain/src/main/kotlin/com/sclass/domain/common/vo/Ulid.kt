package com.sclass.domain.common.vo

import com.github.f4b6a3.ulid.UlidCreator

object Ulid {
    private val PATTERN = Regex("^[0-9A-HJKMNP-TV-Z]{26}$")

    fun generate(): String = UlidCreator.getUlid().toString()

    fun isValid(ulid: String): Boolean {
        if (ulid.length != 26) return false
        return PATTERN.matches(ulid)
    }

    fun parse(ulid: String): String {
        require(isValid(ulid)) { "Invalid ULID: $ulid" }
        return ulid
    }
}
