package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model

import android.os.Parcelable


import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedActivityMapping(
    val exam_id: String,
    val class_id: String,
    val section_id: String,
    val subject_id: String,
    val activity_name: String,
    val selected_column: String,
    val selectedActivityID: String?=null
) : Parcelable
