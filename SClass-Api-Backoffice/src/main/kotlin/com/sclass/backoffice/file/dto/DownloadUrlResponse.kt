package com.sclass.backoffice.file.dto

data class DownloadUrlResponse(
    val downloadUrl: String,
    val originalFilename: String,
    val mimeType: String,
)
