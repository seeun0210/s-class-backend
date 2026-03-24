package com.sclass.supporters.file.dto

data class DownloadUrlResponse(
    val downloadUrl: String,
    val originalFilename: String,
    val mimeType: String,
)
