package com.vs.schoolmessenger.CommonScreens

data class CommonFileData(
    val type: String,
    val path: String,
    val file_name: String? = null,
    val original_file_name: String? = null,

//    Here i have used the original_file_name and file_name as optional values for Payment Proof menu
    )

enum class FileType {
    IMAGE, PDF, DOC, DOCX, EXCEL, PPT, TXT, VIDEO, AUDIO, OTHER
}

data class FileItem(
    val path: String,
    val type: FileType,
    val file_name: String? = null,
    val original_file_name: String? = null,
//    Here i have used the original_file_name and file_name as optional values for Payment Proof menu)

)
