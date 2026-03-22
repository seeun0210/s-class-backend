package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class TeacherDocumentFiles(
    @Column(length = 26)
    val applicationFileId: String? = null,

    @Column(length = 26)
    val completionCertificateFileId: String? = null,

    @Column(length = 26)
    val studentRecordFileId: String? = null,

    @Column(length = 26)
    val residentCertificateFileId: String? = null,

    @Column(length = 26)
    val bankCopyFileId: String? = null,

    @Column(length = 26)
    val policeCheckFileId: String? = null,
)
