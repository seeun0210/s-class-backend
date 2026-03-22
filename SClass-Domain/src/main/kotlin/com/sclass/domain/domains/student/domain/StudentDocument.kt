package com.sclass.domain.domains.student.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.domain.File
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "student_documents")
class StudentDocument(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: Student,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    val file: File,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val documentType: StudentDocumentType,
) : BaseTimeEntity()
