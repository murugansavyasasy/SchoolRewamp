package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Model.Rubric


data class ActivityMark(
    val id: String,
    val name: String,
    val is_edit: Boolean,
    val max_mark: String,
    val selected_name: String,
    val mark: String,
    val rubrics: List<Rubric>? = null
)

//data class ActivityMark(
//    val id: String,
//    val name: String,
//    var mark: String,      // editable
//    var selected_name: String,      // editable
//    val max_mark: String,
//    val is_edit: Boolean
//)
