package com.vs.schoolmessenger.Parent.LSRW.MySubmissionModel

data class ActivityData (
    val id: String,
    val header_id: String,
    val description: String,
    val submitted_date: String,
    val remark: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
)