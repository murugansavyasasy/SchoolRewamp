package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import com.google.gson.annotations.SerializedName

data class TableStructure(
    @SerializedName("table_type")
    val tableType: String,

    @SerializedName("class_info")
    val classInfo: String,

    @SerializedName("school_info")
    val schoolInfo: String,

    @SerializedName("column_headers")
    val columnHeaders: List<ColumnHeader>,

    @SerializedName("estimated_rows")
    val estimatedRows: Int,

    @SerializedName("handwritten_elements")
    val handwrittenElements: List<String>,

    @SerializedName("original_total_columns")
    val originalTotalColumns: Int,

    @SerializedName("selected_columns")
    val selectedColumns: List<String>,

    @SerializedName("extraction_difficulty")
    val extractionDifficulty: String
)