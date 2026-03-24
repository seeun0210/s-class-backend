package com.sclass.supporters.file.usecase

import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.file.exception.FileNotFoundException
import com.sclass.domain.domains.file.service.FileDomainService
import com.sclass.infrastructure.s3.S3Service
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteFileUseCaseTest {
    private lateinit var fileDomainService: FileDomainService
    private lateinit var s3Service: S3Service
    private lateinit var useCase: DeleteFileUseCase

    private val fileId = Ulid.generate()

    @BeforeEach
    fun setUp() {
        fileDomainService = mockk()
        s3Service = mockk()
        useCase = DeleteFileUseCase(fileDomainService, s3Service)
    }

    private fun createFile(): File =
        File.create(
            id = fileId,
            originalFilename = "report.pdf",
            storedFilename = "supporters/plan/2026/03/${fileId}_report.pdf",
            mimeType = "application/pdf",
            fileSize = 1024L,
            fileType = FileType.PLAN,
            uploadedBy = "user-123",
        )

    @Test
    fun `파일을 정상 삭제한다`() {
        val file = createFile()
        every { fileDomainService.delete(fileId) } returns file
        every { s3Service.deleteObject(file.storedFilename) } just runs

        useCase.execute(fileId)

        verify { fileDomainService.delete(fileId) }
        verify { s3Service.deleteObject(file.storedFilename) }
    }

    @Test
    fun `DB 삭제 후 S3 삭제 순서로 실행된다`() {
        val file = createFile()
        every { fileDomainService.delete(fileId) } returns file
        every { s3Service.deleteObject(file.storedFilename) } just runs

        useCase.execute(fileId)

        verifyOrder {
            fileDomainService.delete(fileId)
            s3Service.deleteObject(file.storedFilename)
        }
    }

    @Test
    fun `존재하지 않는 파일 삭제 시 FileNotFoundException 발생`() {
        every { fileDomainService.delete(fileId) } throws FileNotFoundException()

        assertThrows<FileNotFoundException> {
            useCase.execute(fileId)
        }

        verify(exactly = 0) { s3Service.deleteObject(any()) }
    }

    @Test
    fun `S3 삭제 실패 시 예외가 전파된다`() {
        val file = createFile()
        every { fileDomainService.delete(fileId) } returns file
        every { s3Service.deleteObject(file.storedFilename) } throws RuntimeException("S3 error")

        assertThrows<RuntimeException> {
            useCase.execute(fileId)
        }
    }
}
