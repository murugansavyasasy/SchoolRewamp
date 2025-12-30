package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import com.google.gson.annotations.SerializedName

data class TableData(
    @SerializedName("table_structure")
    val tableStructure: TableStructure,
    @SerializedName("review_flags_count")
    val reviewFlagsCount: Int,
    val columns: Int,
    @SerializedName("review_flags")
    val reviewFlags: List<ReviewFlag>,
    @SerializedName("column_mapping")
    val columnMapping: Map<String, String>,
    val records: List<Map<String, Any?>>
)