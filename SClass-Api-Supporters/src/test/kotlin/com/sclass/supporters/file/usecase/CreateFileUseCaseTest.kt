package com.sclass.supporters.file.usecase

import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.file.service.FileService
import com.sclass.infrastructure.s3.S3Service
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateFileUseCaseTest {
    private lateinit var s3Service: S3Service
    private lateinit var fileService: FileService
    private lateinit var useCase: CreateFileUseCase

    @BeforeEach
    fun setUp() {
        s3Service = mockk()
        fileService = mockk()
        useCase = CreateFileUseCase(s3Service, fileService)
    }

    @Test
    fun `presigned URL과 파일 메타를 정상 반환한다`() {
        val fileSlot = slot<File>()
        every { fileService.save(capture(fileSlot)) } answers { fileSlot.captured }
        every { s3Service.generatePresignedPutUrl(any(), any()) } returns "https://s3.example.com/presigned"

        val result =
            useCase.execute(
                uploadedBy = "user-123",
                originalFilename = "report.pdf",
                contentType = "application/pdf",
                fileSize = 1024L,
                fileType = FileType.TASK_SUBMISSION,
            )

        assertEquals("https://s3.example.com/presigned", result.presignedUrl)
        assertTrue(result.fileId.length == 26)
        assertTrue(result.filePath.contains("task_submission"))
        assertTrue(result.filePath.endsWith("_report.pdf"))
    }

    @Test
    fun `저장 경로가 fileType과 날짜 기반으로 생성된다`() {
        val fileSlot = slot<File>()
        every { fileService.save(capture(fileSlot)) } answers { fileSlot.captured }
        every { s3Service.generatePresignedPutUrl(any(), any()) } returns "https://presigned"

        val result =
            useCase.execute(
                uploadedBy = "user-123",
                originalFilename = "photo.jpg",
                contentType = "image/jpeg",
                fileSize = 2048L,
                fileType = FileType.TEACHER_PROFILE,
            )

        val today = LocalDate.now()
        val datePath = today.format(DateTimeFormatter.ofPattern("yyyy/MM"))
        assertTrue(result.filePath.startsWith("supporters/teacher_profile/$datePath/"))
    }

    @Test
    fun `File 엔티티가 올바른 값으로 저장된다`() {
        val fileSlot = slot<File>()
        every { fileService.save(capture(fileSlot)) } answers { fileSlot.captured }
        every { s3Service.generatePresignedPutUrl(any(), any()) } returns "https://presigned"

        useCase.execute(
            uploadedBy = "user-456",
            originalFilename = "doc.pdf",
            contentType = "application/pdf",
            fileSize = 5000L,
            fileType = FileType.PLAN,
        )

        val saved = fileSlot.captured
        assertEquals("doc.pdf", saved.originalFilename)
        assertEquals("application/pdf", saved.mimeType)
        assertEquals(5000L, saved.fileSize)
        assertEquals(FileType.PLAN, saved.fileType)
        assertEquals("user-456", saved.uploadedBy)
        assertTrue(saved.storedFilename.endsWith("_doc.pdf"))
        assertTrue(saved.filePath.contains("plan"))
    }

    @Test
    fun `S3Service에 올바른 key와 contentType이 전달된다`() {
        val fileSlot = slot<File>()
        every { fileService.save(capture(fileSlot)) } answers { fileSlot.captured }
        every { s3Service.generatePresignedPutUrl(any(), any()) } returns "https://presigned"

        val result =
            useCase.execute(
                uploadedBy = "user-789",
                originalFilename = "image.png",
                contentType = "image/png",
                fileSize = 3000L,
                fileType = FileType.MATERIAL,
            )

        verify {
            s3Service.generatePresignedPutUrl(
                key = result.filePath,
                contentType = "image/png",
            )
        }
    }
}
