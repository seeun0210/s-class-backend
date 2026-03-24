package com.sclass.domain.domains.teacher.domain

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
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "teacher_documents")
class TeacherDocument(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    val teacher: Teacher,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val file: File,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val documentType: TeacherDocumentType,
) : BaseTimeEntity()
