package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data


data class MarkColumn(
    val subjectId: String,
    val subjectName: String,
    val activityId: String,
    val activityName: String,
    val selected_name: String,
    val maxMark: Int,
    val isRubric: Boolean = false,
    val rubricId: String? = null,
    val rubricName: String? = null,
    val parentActivityName: String? = null
)


//data class MarkColumn(
//    val subjectId: String,
//    val subjectName: String,
//    val activityId: String,
//    val activityName: String,
//    val selected_name: String,
//    val maxMark: Int,
//)
