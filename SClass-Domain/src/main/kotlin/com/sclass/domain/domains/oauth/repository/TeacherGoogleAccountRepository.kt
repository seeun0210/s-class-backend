package com.sclass.domain.domains.oauth.repository

import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherGoogleAccountRepository : JpaRepository<TeacherGoogleAccount, String> {
    fun findByUserId(userId: String): TeacherGoogleAccount?

    fun deleteByUserId(userId: String)

    fun existsByUserId(userId: String): Boolean
}
