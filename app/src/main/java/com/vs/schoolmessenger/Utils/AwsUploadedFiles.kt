package com.vs.schoolmessenger.Utils

data class AwsUploadedFiles(
    val isFileUrl: String,
    var isFileType: String,
    val originalFileName: String? = null
)
