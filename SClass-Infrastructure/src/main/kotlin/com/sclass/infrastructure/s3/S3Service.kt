package com.sclass.infrastructure.s3

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Service
@ConditionalOnProperty("cloud.aws.s3.bucket")
class S3Service(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val s3Properties: S3Properties,
) {
    fun generatePresignedPutUrl(
        key: String,
        contentType: String,
        expiration: Duration = Duration.ofMinutes(30),
    ): String {
        val putObjectRequest =
            PutObjectRequest
                .builder()
                .bucket(s3Properties.bucket)
                .key(key)
                .contentType(contentType)
                .build()

        val presignRequest =
            PutObjectPresignRequest
                .builder()
                .signatureDuration(expiration)
                .putObjectRequest(putObjectRequest)
                .build()

        return s3Presigner.presignPutObject(presignRequest).url().toString()
    }

    fun generatePresignedGetUrl(
        key: String,
        expiration: Duration = Duration.ofHours(1),
    ): String {
        val getObjectRequest =
            GetObjectRequest
                .builder()
                .bucket(s3Properties.bucket)
                .key(key)
                .build()

        val presignRequest =
            GetObjectPresignRequest
                .builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }

    fun deleteObject(key: String) {
        val deleteRequest =
            DeleteObjectRequest
                .builder()
                .bucket(s3Properties.bucket)
                .key(key)
                .build()

        s3Client.deleteObject(deleteRequest)
    }

    fun getPublicUrl(key: String): String {
        require(key.startsWith(PUBLIC_PREFIX)) {
            "Public URL is only available for keys under '$PUBLIC_PREFIX' prefix (got: $key)"
        }
        val baseUrl = requireNotNull(s3Properties.publicBaseUrl) { "cloud.aws.s3.public-base-url is not configured" }
        return "${baseUrl.trimEnd('/')}/${key.removePrefix(PUBLIC_PREFIX)}"
    }

    companion object {
        const val PUBLIC_PREFIX = "public/"
    }
}
