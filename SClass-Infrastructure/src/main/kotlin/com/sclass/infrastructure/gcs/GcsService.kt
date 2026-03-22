package com.sclass.infrastructure.gcs

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
@ConditionalOnProperty("cloud.gcp.storage.project-id")
class GcsService(
    private val storage: Storage,
    private val gcsProperties: GcsProperties,
) {
    fun generatePresignedPutUrl(
        key: String,
        contentType: String,
        expiration: Duration = Duration.ofMinutes(30),
    ): String {
        val blobInfo =
            BlobInfo
                .newBuilder(BlobId.of(gcsProperties.bucket, key))
                .setContentType(contentType)
                .build()

        return storage
            .signUrl(
                blobInfo,
                expiration.toSeconds(),
                TimeUnit.SECONDS,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature(),
            ).toString()
    }

    fun generatePresignedGetUrl(
        key: String,
        expiration: Duration = Duration.ofHours(1),
    ): String {
        val blobInfo =
            BlobInfo
                .newBuilder(BlobId.of(gcsProperties.bucket, key))
                .build()

        return storage
            .signUrl(
                blobInfo,
                expiration.toSeconds(),
                TimeUnit.SECONDS,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature(),
            ).toString()
    }

    fun deleteObject(key: String) {
        storage.delete(BlobId.of(gcsProperties.bucket, key))
    }

    fun fileExists(key: String): Boolean {
        val blob = storage.get(BlobId.of(gcsProperties.bucket, key))
        return blob != null && blob.exists()
    }

    fun getFileSize(key: String): Long? {
        val blob = storage.get(BlobId.of(gcsProperties.bucket, key)) ?: return null
        return if (blob.exists()) blob.size else null
    }
}
