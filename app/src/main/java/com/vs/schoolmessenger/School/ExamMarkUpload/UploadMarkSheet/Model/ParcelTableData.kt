package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelTableData(
    @SerializedName("table_structure")
    val tableStructure: TableStructure? = null,
    @SerializedName("review_flags_count")
    val reviewFlagsCount: Int = 0,
    val columns: Int = 0,
    @SerializedName("review_flags")
    val reviewFlags: List<ReviewFlag> = emptyList(),
    @SerializedName("column_mapping")
    val columnMapping: Map<String, String> = emptyMap(),
    val records: List<Map<String, String>> = emptyList()
) : Parcelable




