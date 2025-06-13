package com.vs.schoolmessenger.CommonScreens

//data class CommonFileData(
//    val type: String,
//    val path: String,
//)


data class CommonFileData(
    val type: String,
    val path: String
)

enum class FileType {
    IMAGE, PDF, DOC, DOCX, EXCEL, PPT, TXT, VIDEO, AUDIO, OTHER
}

data class FileItem(
    val path: String,
    val type: FileType
)


