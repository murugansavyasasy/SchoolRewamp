package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class getActivitySubjectNameData(
    val section_id: String,
    val section_name: String,
    val class_id: String,
    val class_name: String,
    val subject_id: String,
    val subject: String,
    val paper: List<getActivityPaperNameData>,
    var isSelected: Boolean = false

) : Parcelable