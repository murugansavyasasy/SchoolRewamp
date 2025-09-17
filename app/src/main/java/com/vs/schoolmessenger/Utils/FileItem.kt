package com.vs.schoolmessenger.Utils


enum class FileType {
//    IMAGE,
//    PDF,
//    DOC,
//    DOCX,
//    EXCEL,
//    PPT,
//    TXT,
//    VIDEO,
//    AUDIO,
//    OTHER

    IMAGE,
    VIDEO,
    AUDIO,
    EXCEL,
    PDF,
    DOC,       // for .doc
    DOCX,      // for .docx
    PPT,
    PPTX,
    XLS,
    XLSX,
    TXT,
    OTHER
}

data class FileItem(
    val path: String,
    val type: FileType
)
