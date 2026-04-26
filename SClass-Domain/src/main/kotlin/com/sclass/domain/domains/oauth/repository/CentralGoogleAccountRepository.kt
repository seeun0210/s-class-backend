package com.sclass.domain.domains.oauth.repository

import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import org.springframework.data.jpa.repository.JpaRepository

interface CentralGoogleAccountRepository : JpaRepository<CentralGoogleAccount, String>
