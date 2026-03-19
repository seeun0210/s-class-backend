package com.sclass.domain.domains.file.repository

import com.sclass.domain.domains.file.domain.File
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<File, String> {
    fun findAllByIdIn(ids: List<String>): List<File>
}
