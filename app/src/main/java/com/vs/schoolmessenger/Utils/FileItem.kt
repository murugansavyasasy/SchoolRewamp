package com.vs.schoolmessenger.Utils


enum class FileType {
    IMAGE,
    PDF,
    DOC,
    DOCX,
    EXCEL,
    PPT,
    TXT,
    VIDEO,
    AUDIO,
    OTHER
}


data class FileItem(
    val path:   String,
    val type:   FileType
)