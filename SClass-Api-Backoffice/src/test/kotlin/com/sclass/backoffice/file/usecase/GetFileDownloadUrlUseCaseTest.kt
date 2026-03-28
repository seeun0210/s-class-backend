package com.sclass.backoffice.file.usecase

import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.file.exception.FileNotFoundException
import com.sclass.infrastructure.s3.S3Service
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetFileDownloadUrlUseCaseTest {
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var s3Service: S3Service
    private lateinit var useCase: GetFileDownloadUrlUseCase

    private val fileId = Ulid.generate()

    @BeforeEach
    fun setUp() {
        fileAdaptor = mockk()
        s3Service = mockk()
        useCase = GetFileDownloadUrlUseCase(fileAdaptor, s3Service)
    }

    @Test
    fun `다운로드 URL을 정상 반환한다`() {
        val file =
            File.create(
                id = fileId,
                originalFilename = "report.pdf",
                storedFilename = "backoffice/plan/2026/03/${fileId}_report.pdf",
                mimeType = "application/pdf",
                fileSize = 1024L,
                fileType = FileType.PLAN,
                uploadedBy = "user-123",
            )
        every { fileAdaptor.findById(fileId) } returns file
        every { s3Service.generatePresignedGetUrl(key = file.storedFilename) } returns "https://s3.example.com/download"

        val result = useCase.execute(fileId)

        assertEquals("https://s3.example.com/download", result.downloadUrl)
        assertEquals("report.pdf", result.originalFilename)
        assertEquals("application/pdf", result.mimeType)
    }

    @Test
    fun `존재하지 않는 파일 조회 시 FileNotFoundException 발생`() {
        every { fileAdaptor.findById(fileId) } throws FileNotFoundException()

        assertThrows<FileNotFoundException> {
            useCase.execute(fileId)
        }
    }
}
