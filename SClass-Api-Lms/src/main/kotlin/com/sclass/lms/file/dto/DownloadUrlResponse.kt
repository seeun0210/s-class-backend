package com.sclass.lms.file.dto

data class DownloadUrlResponse(
    val downloadUrl: String,
    val originalFilename: String,
    val mimeType: String,
)
