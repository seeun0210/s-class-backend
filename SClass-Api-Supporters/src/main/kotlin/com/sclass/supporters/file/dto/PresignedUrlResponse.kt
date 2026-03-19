package com.sclass.supporters.file.dto

data class PresignedUrlResponse(
    val fileId: String,
    val presignedUrl: String,
    val storedFilename: String,
)
